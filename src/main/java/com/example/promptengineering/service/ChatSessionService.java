package com.example.promptengineering.service;

import com.example.promptengineering.dto.SessionChatRequest;
import com.example.promptengineering.entity.Chat;
import com.example.promptengineering.entity.SharedKey;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
@Setter
public class ChatSessionService {

    private static final int MAX_TOOL_ITERATIONS = 8;

    private final ChatSessionRegistry registry;
    private final ToolRegistry toolRegistry;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final SharedKeyService sharedKeyService;
    private final EncryptionService encryptionService;
    private final ChatSessionPersistenceService persistence;
    private volatile Double temperature;
    private volatile String reasoningEffort;
    private volatile Map<String, Object> providerConfig;

    private ChatSession ensureHydrated(ChatSession chatSession, User user) {
        if (chatSession.isHydrated())
            return chatSession;
        Chat chat = persistence.ensureChat(chatSession.getChatUuid(), user);
        chatSession.setChatEntity(chat);
        if (chatSession.getMessages().isEmpty()) {
            chatSession.getMessages().addAll(persistence.loadMessages(chat));
        }
        chatSession.setHydrated(true);
        return chatSession;
    }

    public Flux<ServerSentEvent<String>> process(User user, UUID chatUuid,
                                                 SessionChatRequest req) {
        return Mono.fromCallable(() -> {
            ChatSession chatSession = registry.getOrCreate(chatUuid, user.getId());
            ensureHydrated(chatSession, user);
            applyConfig(chatSession, req);

            long start = req.start() != null ? req.start() : System.currentTimeMillis();
            SessionMessage sm = chatSession.addUserMessage(
                    textMessage("user", req.text()), req.documents(), req.images(),
                    start);
            persistence.save(chatSession.getChatEntity(), sm);
            return chatSession;
        }).subscribeOn(Schedulers.boundedElastic()).flatMapMany(s -> {
            StreamingStats stats = new StreamingStats();
            return runAgentLoop(s, user, 0, stats);
        });
    }

    private void applyConfig(ChatSession chatSession, SessionChatRequest req) {
        if (req.model() != null)
            chatSession.setModel(req.model());
        if (req.provider() != null)
            chatSession.setProvider(req.provider());
        if (req.providerStrategy() != null)
            chatSession.setProviderStrategy(req.providerStrategy());
        if (req.systemPrompt() != null)
            chatSession.setSystemPrompt(req.systemPrompt());
        if (req.url() != null)
            chatSession.setUrl(req.url());
        if (req.useSharedKeys() != null)
            chatSession.setUseSharedKeys(req.useSharedKeys());
        chatSession.setEnabledTools(req.enabledTools() != null
                ? new HashSet<>(req.enabledTools())
                : new HashSet<>());

        if (req.temperature() != null)
            chatSession.setTemperature(req.temperature());
        if (req.reasoningEffort() != null)
            chatSession.setReasoningEffort(req.reasoningEffort());
        if (req.providerConfig() != null)
            chatSession.setProviderConfig(req.providerConfig());
    }

    private Flux<ServerSentEvent<String>> runAgentLoop(ChatSession session, User user,
                                                       int depth, StreamingStats stats) {
        if (depth >= MAX_TOOL_ITERATIONS) {
            return Flux.just(errorEvent("Max tool iterations reached"));
        }

        RequestBuilder requestBuilder;
        try {
            requestBuilder = buildRequest(session, user);
        } catch (Exception e) {
            return Flux.just(errorEvent(e.getMessage()));
        }

        final long assistantStart = System.currentTimeMillis();
        ToolCallAccumulator toolCalls = new ToolCallAccumulator(objectMapper);
        AtomicBoolean saved = new AtomicBoolean(false);

        Runnable saveMessage = () -> {
            if (saved.compareAndSet(false, true)) {
                long assistantEnd = System.currentTimeMillis();
                List<ToolCall> calls = toolCalls.completedCalls().stream().filter(
                        call -> session.getEnabledTools().contains(call.getName()))
                        .toList();

                String reasoningText = toolCalls.getReasoning();
                String contentText = toolCalls.getContent();

                if ((contentText != null && !contentText.isEmpty())
                        || (reasoningText != null && !reasoningText.isEmpty())
                        || !calls.isEmpty()) {
                    SessionMessage asm = session.addAssistantMessage(
                            assistantMessage(contentText, calls), reasoningText,
                            assistantStart, assistantEnd);
                    try {
                        persistence.save(session.getChatEntity(), asm);
                    } catch (Exception e) {
                        log.error("Failed to save assistant message", e);
                    }
                }
            }
        };

        Flux<ServerSentEvent<String>> tokens = chatService
                .makeRequest(requestBuilder, user).map(ServerSentEvent::data)
                .filter(Objects::nonNull).doOnSubscribe(s -> stats.beginLlmCall())
                .doFinally(sig -> {
                    stats.endLlmCall();
                    Mono.fromRunnable(saveMessage)
                            .subscribeOn(Schedulers.boundedElastic()).subscribe();
                }).concatMap(data -> {
                    ToolCallAccumulator.Chunk chunk = toolCalls.consume(data);
                    if (chunk.hasError()) {
                        return Flux.error(new RuntimeException(chunk.error()));
                    }

                    List<ServerSentEvent<String>> out = new ArrayList<>(3);

                    if (chunk.hasContent()) {
                        stats.addContent(chunk.content().length());
                        out.add(event("token", Map.of("text", chunk.content())));
                    }
                    if (chunk.hasReasoning()) {
                        stats.addReasoning(chunk.reasoning().length());
                        out.add(event("reasoning", Map.of("text", chunk.reasoning())));
                    }
                    if (stats.shouldEmit()) {
                        out.add(event("stats", stats.snapshot()));
                        stats.markEmit();
                    }

                    return Flux.fromIterable(out);
                });

        Flux<ServerSentEvent<String>> continuation = Flux.defer(() -> {
            List<ToolCall> calls = toolCalls.completedCalls().stream()
                    .filter(call -> session.getEnabledTools().contains(call.getName()))
                    .toList();

            if (calls.isEmpty()) {
                Map<String, Object> doneData = new HashMap<>(stats.snapshot());
                doneData.put("finishReason",
                        toolCalls.getFinishReason() != null
                                ? toolCalls.getFinishReason()
                                : "stop");
                return Flux.just(event("done", doneData));
            }
            return Flux.fromIterable(calls)
                    .concatMap(call -> executeAndEmit(call, session)).concatWith(Flux
                            .defer(() -> runAgentLoop(session, user, depth + 1, stats)));
        });

        return Flux.concat(tokens, continuation)
                .onErrorResume(e -> Flux.just(errorEvent(e.getMessage())));
    }

    private Flux<ServerSentEvent<String>> executeAndEmit(ToolCall call,
                                                         ChatSession session) {
        Flux<ServerSentEvent<String>> callEvent = Flux
                .just(event("tool_call", Map.of("id", call.getId(), "name",
                        call.getName(), "arguments", call.getArguments())));

        Mono<ServerSentEvent<String>> resultEvent = Mono.fromCallable(() -> {
            ToolExecutor exec = toolRegistry.getExecutor(call.getName()).orElseThrow(
                    () -> new IllegalStateException("Unknown tool: " + call.getName()));
            Map<String, Object> args = parseJson(call.getArguments());
            String result = exec.execute(args);
            SessionMessage tsm = session
                    .addToolMessage(toolMessage(call.getId(), call.getName(), result));
            persistence.save(session.getChatEntity(), tsm);
            return event("tool_result", Map.of("id", call.getId(), "name", call.getName(),
                    "content", result));
        }).subscribeOn(Schedulers.boundedElastic()).onErrorResume(e -> {
            String err = "Tool '" + call.getName() + "' failed: " + e.getMessage();
            log.warn(err, e);
            session.addToolMessage(toolMessage(call.getId(), call.getName(), err));

            return Mono.just(event("tool_error",
                    Map.of("id", call.getId(), "name", call.getName(), "error", err)));
        });

        return callEvent.concatWith(resultEvent);
    }

    private Message toProviderMessage(SessionMessage sessionMessage) {

        Message original = sessionMessage.getProviderMessage();

        if (sessionMessage.getImages() == null || sessionMessage.getImages().isEmpty()) {
            return original;
        }

        Message clone = new Message();
        clone.setRole(original.getRole());
        clone.setContent(new ArrayList<>(original.getContent()));
        clone.setToolCalls(original.getToolCalls());
        clone.setToolCallId(original.getToolCallId());
        clone.setToolName(original.getToolName());
        clone.setStringContent(original.getStringContent());
        clone.setCached(original.isCached());

        if (!sessionMessage.getDocuments().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String doc : sessionMessage.getDocuments()) {
                sb.append("<files>").append(doc).append("</files>");
            }
            TextContent docContent = new TextContent();
            docContent.setType("text");
            docContent.setText(sb.toString());
            clone.getContent().addFirst(docContent);
        }

        for (String imgId : sessionMessage.getImages()) {
            try {
                ImageContent ic = new ImageContent();
                ic.setType("image");
                ic.setFileId(Long.parseLong(imgId));
                clone.getContent().add(ic);
            } catch (NumberFormatException e) {
                log.warn("Invalid image file ID in session {}: {}",
                        sessionMessage.getUuid(), imgId);
            }
        }
        return clone;
    }

    private RequestBuilder buildRequest(ChatSession session, User user) {
        if (session.getModel() == null)
            throw new IllegalStateException("Model not set");
        if (session.getUrl() == null)
            throw new IllegalStateException("URL not set");

        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.setModel(session.getModel());
        requestBuilder.setUrl(session.getUrl());
        requestBuilder.setStream(true);
        requestBuilder.setProvider(session.getProvider());
        if (session.getProviderStrategy() != null)
            requestBuilder.setProviderStrategy(session.getProviderStrategy());
        if (session.getSystemPrompt() != null)
            requestBuilder.setSystem(session.getSystemPrompt());

        boolean advanced = false;
        if (session.getTemperature() != null) {
            requestBuilder.setTemperature(session.getTemperature());
            advanced = true;
        }
        if (session.getMaxTokens() != null) {
            requestBuilder.setMaxTokens(session.getMaxTokens());
            advanced = true;
        }
        if (session.getTopP() != null) {
            requestBuilder.setTop_p(session.getTopP());
            advanced = true;
        }
        if (session.getFrequencyPenalty() != null) {
            requestBuilder.setFrequencyPenalty(session.getFrequencyPenalty());
            advanced = true;
        }
        if (session.getPresencePenalty() != null) {
            requestBuilder.setPresencePenalty(session.getPresencePenalty());
            advanced = true;
        }
        if (session.getReasoningEffort() != null) {
            requestBuilder.setReasoningEffort(session.getReasoningEffort());
        }
        if (session.getProviderConfig() != null) {
            requestBuilder.setProviderConfig(session.getProviderConfig());
        }

        requestBuilder.includeAdvancedParams(advanced);

        requestBuilder.setMessages(session.getMessages().stream()
                .map(this::toProviderMessage).collect(java.util.stream.Collectors
                        .toCollection(java.util.ArrayList::new)));

        String providerKey = session.getProvider().toUpperCase();
        String apiKey;
        if (session.isUseSharedKeys()) {
            SharedKey sk = sharedKeyService.getRandomWorkingKeyEntity(providerKey)
                    .orElseThrow(() -> new IllegalStateException(
                            "No shared key for " + providerKey));
            apiKey = encryptionService.decrypt(sk.getKeyValue());
            requestBuilder.setUseSharedKeys(true);
            requestBuilder.setSharedKeyId(sk.getId());
        } else {
            apiKey = userService.getUserKeys(user).get(providerKey);
            if (apiKey == null || apiKey.isBlank())
                throw new IllegalStateException("Missing API key for " + providerKey);
        }
        requestBuilder.setKey(apiKey);

        List<String> enabled = new ArrayList<>(session.getEnabledTools());
        if (!enabled.isEmpty()) {
            List<ToolDefinition> defs = toolRegistry.resolve(enabled);
            if (!defs.isEmpty()) {
                requestBuilder.setTools(
                        defs.stream().map(ToolDefinition::toOpenAiFormat).toList());
            }
        }

        return requestBuilder;
    }

    private Message textMessage(String role, String text) {
        Message message = new Message(role, List.of(new TextContent(text)));
        return message;
    }

    private Message assistantMessage(String content, List<ToolCall> toolCalls) {
        Message message = new Message();
        message.setRole("assistant");
        message.setContent(content == null || content.isEmpty()
                ? List.of()
                : List.of(new TextContent(content)));
        if (toolCalls != null && !toolCalls.isEmpty())
            message.setToolCalls(toolCalls);
        return message;
    }

    private Message toolMessage(String toolCallId, String name, String content) {
        Message message = new Message();
        message.setRole("tool");
        message.setToolCallId(toolCallId);
        message.setToolName(name);
        message.setStringContent(content);
        message.setContent(List.of());
        return message;
    }

    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isBlank())
            return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid tool arguments: " + json, e);
        }
    }

    private ServerSentEvent<String> event(String name, Map<String, ?> data) {
        try {
            return ServerSentEvent.<String>builder().event(name)
                    .data(objectMapper.writeValueAsString(data)).build();
        } catch (Exception e) {
            return ServerSentEvent.<String>builder().event("error")
                    .data("{\"error\":\"serialization\"}").build();
        }
    }

    private ServerSentEvent<String> errorEvent(String msg) {
        return event("error", Map.of("error", msg));
    }

    private static class StreamingStats {
        final long processStartMs = System.currentTimeMillis();
        volatile long llmStartMs = 0;
        volatile long llmElapsedMs = 0;

        volatile long contentChars = 0;
        volatile long reasoningChars = 0;
        volatile long charsSinceLastEmit = 0;
        volatile long lastEmitMs = processStartMs;

        volatile long lastReasoningAtMs = 0;
        volatile long lastContentAtMs = 0;

        private static final long WINDOW_MS = 2000;
        private static final long MIN_WINDOW_MS = 1000;
        private final java.util.ArrayDeque<long[]> samples = new java.util.ArrayDeque<>();
        private long charsInWindow = 0;

        void beginLlmCall() {
            llmStartMs = System.currentTimeMillis();
        }

        void endLlmCall() {
            if (llmStartMs > 0) {
                llmElapsedMs += System.currentTimeMillis() - llmStartMs;
                llmStartMs = 0;
            }
        }

        long currentLlmElapsed() {
            return llmStartMs > 0
                    ? llmElapsedMs + (System.currentTimeMillis() - llmStartMs)
                    : llmElapsedMs;
        }

        boolean isCurrentlyThinking() {
            long now = System.currentTimeMillis();
            return lastReasoningAtMs > 0 && lastReasoningAtMs >= lastContentAtMs
                    && (now - lastReasoningAtMs) < 400;
        }

        void addContent(int n) {
            long now = System.currentTimeMillis();
            lastContentAtMs = now;
            contentChars += n;
            charsSinceLastEmit += n;
            addSample(now, n);
        }

        void addReasoning(int n) {
            long now = System.currentTimeMillis();
            lastReasoningAtMs = now;
            reasoningChars += n;
            charsSinceLastEmit += n;
            addSample(now, n);
        }

        private void addSample(long now, int chars) {
            samples.addLast(new long[]{now, chars});
            charsInWindow += chars;
            trimSamples(now);
        }

        private void trimSamples(long now) {
            while (!samples.isEmpty() && now - samples.peekFirst()[0] > WINDOW_MS) {
                charsInWindow -= samples.pollFirst()[1];
            }
        }

        double currentTps() {
            long now = System.currentTimeMillis();
            trimSamples(now);
            if (samples.isEmpty())
                return 0.0;

            long windowStart = samples.peekFirst()[0];
            long windowMs = now - windowStart;
            long useMs = Math.max(windowMs, MIN_WINDOW_MS);

            return (charsInWindow / 4.0) * 1000.0 / useMs;
        }

        boolean shouldEmit() {
            return System.currentTimeMillis() - lastEmitMs >= 500
                    && charsSinceLastEmit > 0;
        }

        void markEmit() {
            lastEmitMs = System.currentTimeMillis();
            charsSinceLastEmit = 0;
        }

        Map<String, Object> snapshot() {
            long wallMs = Math.max(System.currentTimeMillis() - processStartMs, 1);
            long llmMs = Math.max(currentLlmElapsed(), 1);

            double cTok = contentChars / 4.0;
            double rTok = reasoningChars / 4.0;
            double total = cTok + rTok;

            Map<String, Object> m = new HashMap<>();
            m.put("elapsedMs", wallMs);
            m.put("llmElapsedMs", llmMs);
            m.put("contentChars", contentChars);
            m.put("reasoningChars", reasoningChars);
            m.put("contentTokens", Math.round(cTok));
            m.put("reasoningTokens", Math.round(rTok));
            m.put("totalTokens", Math.round(total));
            m.put("tps", round2(currentTps()));
            m.put("avgTps", round2(total * 1000.0 / llmMs));
            m.put("inThinking", isCurrentlyThinking());
            return m;
        }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
