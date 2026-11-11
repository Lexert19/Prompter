package com.example.promptengineering.service;

import com.example.promptengineering.dto.SessionChatRequest;
import com.example.promptengineering.entity.Chat;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.RequestBuilder;
import com.example.promptengineering.model.ToolCall;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
@Setter
public class ChatSessionService {

    private static final int MAX_TOOL_ITERATIONS = 8;

    private final ChatSessionRegistry registry;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    private final ChatSessionPersistenceService persistence;
    private final MetricsService metricsService;
    private final SessionMessageMapper messageMapper;
    private final ChatRequestFactory requestFactory;
    private final ToolExecutionService toolExecutionService;
    private final SseEventFactory sse;

    private volatile Double temperature;
    private volatile String reasoningEffort;
    private volatile Map<String, Object> providerConfig;

    public Flux<ServerSentEvent<String>> process(User user, UUID chatUuid,
                                                 SessionChatRequest req) {
        return Mono.fromCallable(() -> prepareSession(user, chatUuid, req))
                .subscribeOn(Schedulers.boundedElastic()).flatMapMany(
                        session -> runAgentLoop(session, user, 0, new StreamingStats()));
    }

    private ChatSession prepareSession(User user, UUID chatUuid, SessionChatRequest req) {
        ChatSession session = registry.getOrCreate(chatUuid, user.getId());
        ensureHydrated(session, user);
        applyConfig(session, req);

        long start = req.start() != null ? req.start() : System.currentTimeMillis();
        SessionMessage userMessage = session.addUserMessage(
                messageMapper.textMessage("user", req.text()), req.documents(),
                req.images(), start);
        persistence.save(session.getChatEntity(), userMessage);
        return session;
    }

    private void ensureHydrated(ChatSession session, User user) {
        if (session.isHydrated())
            return;
        Chat chat = persistence.ensureChat(session.getChatUuid(), user);
        session.setChatEntity(chat);
        if (session.getMessages().isEmpty()) {
            session.getMessages().addAll(persistence.loadMessages(chat));
        }
        session.setHydrated(true);
    }

    private void applyConfig(ChatSession session, SessionChatRequest req) {
        if (req.model() != null)
            session.setModel(req.model());
        if (req.provider() != null)
            session.setProvider(req.provider());
        if (req.providerStrategy() != null) {
            session.setProviderStrategy(req.providerStrategy());
        }
        if (req.systemPrompt() != null)
            session.setSystemPrompt(req.systemPrompt());
        if (req.url() != null)
            session.setUrl(req.url());
        if (req.useSharedKeys() != null)
            session.setUseSharedKeys(req.useSharedKeys());
        session.setEnabledTools(req.enabledTools() != null
                ? new HashSet<>(req.enabledTools())
                : new HashSet<>());
        if (req.temperature() != null)
            session.setTemperature(req.temperature());
        if (req.reasoningEffort() != null) {
            session.setReasoningEffort(req.reasoningEffort());
        }
        if (req.providerConfig() != null) {
            session.setProviderConfig(req.providerConfig());
        }
    }

    private Flux<ServerSentEvent<String>> runAgentLoop(ChatSession session, User user,
                                                       int depth, StreamingStats stats) {
        if (depth >= MAX_TOOL_ITERATIONS) {
            return Flux.just(sse.error("Max tool iterations reached"));
        }

        RequestBuilder requestBuilder;
        try {
            requestBuilder = requestFactory.build(session, user);
        } catch (Exception e) {
            return Flux.just(sse.error(e.getMessage()));
        }

        long assistantStart = System.currentTimeMillis();
        ToolCallAccumulator toolCalls = new ToolCallAccumulator(objectMapper);
        AtomicBoolean assistantSaved = new AtomicBoolean(false);

        Flux<ServerSentEvent<String>> tokens = streamLlmTurn(requestBuilder, user,
                toolCalls, stats, () -> saveAssistantMessage(session, toolCalls, stats,
                        assistantStart, assistantSaved));

        Flux<ServerSentEvent<String>> continuation = Flux
                .defer(() -> continueOrFinish(session, user, toolCalls, depth, stats));

        return Flux.concat(tokens, continuation)
                .onErrorResume(e -> Flux.just(sse.error(e.getMessage())));
    }

    private Flux<ServerSentEvent<String>> streamLlmTurn(RequestBuilder requestBuilder,
                                                        User user,
                                                        ToolCallAccumulator toolCalls,
                                                        StreamingStats stats,
                                                        Runnable onComplete) {
        return chatService.makeRequest(requestBuilder, user).map(ServerSentEvent::data)
                .filter(Objects::nonNull).doOnSubscribe(s -> stats.beginLlmCall())
                .doFinally(sig -> {
                    stats.endLlmCall();
                    Mono.fromRunnable(onComplete).subscribeOn(Schedulers.boundedElastic())
                            .subscribe();
                }).concatMap(data -> processLlmChunk(data, toolCalls, stats));
    }

    private Flux<ServerSentEvent<String>> processLlmChunk(String data,
                                                          ToolCallAccumulator toolCalls,
                                                          StreamingStats stats) {
        ToolCallAccumulator.Chunk chunk = toolCalls.consume(data);
        if (chunk.hasError()) {
            return Flux.error(new RuntimeException(chunk.error()));
        }

        List<ServerSentEvent<String>> out = new ArrayList<>(3);
        if (chunk.hasContent()) {
            stats.addContent(chunk.content().length());
            out.add(sse.event("token", Map.of("text", chunk.content())));
        }
        if (chunk.hasReasoning()) {
            stats.addReasoning(chunk.reasoning().length());
            out.add(sse.event("reasoning", Map.of("text", chunk.reasoning())));
        }
        if (stats.shouldEmit()) {
            out.add(sse.event("stats", stats.snapshot()));
            stats.markEmit();
        }
        return Flux.fromIterable(out);
    }

    private Flux<ServerSentEvent<String>> continueOrFinish(ChatSession session, User user,
                                                           ToolCallAccumulator toolCalls,
                                                           int depth,
                                                           StreamingStats stats) {
        List<ToolCall> calls = enabledCalls(toolCalls, session);
        if (calls.isEmpty()) {
            return Flux.just(buildDoneEvent(toolCalls, stats));
        }
        return Flux.fromIterable(calls)
                .concatMap(call -> toolExecutionService.execute(call, session, user))
                .concatWith(
                        Flux.defer(() -> runAgentLoop(session, user, depth + 1, stats)));
    }

    private List<ToolCall> enabledCalls(ToolCallAccumulator toolCalls,
                                        ChatSession session) {
        return toolCalls.completedCalls().stream()
                .filter(call -> session.getEnabledTools().contains(call.getName()))
                .toList();
    }

    private ServerSentEvent<String> buildDoneEvent(ToolCallAccumulator toolCalls,
                                                   StreamingStats stats) {
        Map<String, Object> data = new HashMap<>(stats.snapshot());
        data.put("finishReason",
                toolCalls.getFinishReason() != null
                        ? toolCalls.getFinishReason()
                        : "stop");
        return sse.event("done", data);
    }

    private void saveAssistantMessage(ChatSession session, ToolCallAccumulator toolCalls,
                                      StreamingStats stats, long assistantStart,
                                      AtomicBoolean alreadySaved) {
        if (!alreadySaved.compareAndSet(false, true))
            return;

        long assistantEnd = System.currentTimeMillis();
        List<ToolCall> calls = enabledCalls(toolCalls, session);
        String content = toolCalls.getContent();
        String reasoning = toolCalls.getReasoning();

        if (!hasAnyContent(content, reasoning, calls))
            return;

        SessionMessage assistantMessage = session.addAssistantMessage(
                messageMapper.assistantMessage(content, calls), reasoning, assistantStart,
                assistantEnd);

        try {
            persistence.save(session.getChatEntity(), assistantMessage);
        } catch (Exception e) {
            log.error("Failed to save assistant message", e);
        }

        recordChatMetrics(stats.snapshot(), calls.size());
    }

    private static boolean hasAnyContent(String content, String reasoning,
                                         List<ToolCall> calls) {
        return (content != null && !content.isEmpty())
                || (reasoning != null && !reasoning.isEmpty()) || !calls.isEmpty();
    }

    private void recordChatMetrics(Map<String, Object> s, int toolCallCount) {
        metricsService.record("chat.tokens.content", num(s, "contentTokens"));
        metricsService.record("chat.tokens.reasoning", num(s, "reasoningTokens"));
        metricsService.record("chat.tokens.total", num(s, "totalTokens"));
        metricsService.record("chat.latency.llm.ms", num(s, "llmElapsedMs"));
        metricsService.record("chat.latency.wall.ms", num(s, "elapsedMs"));
        metricsService.record("chat.tps.avg", num(s, "avgTps"));
        metricsService.record("chat.tool.calls", toolCallCount);
        metricsService.record("chat.requests", 1.0);
    }

    private static double num(Map<String, Object> m, String key) {
        return ((Number) m.get(key)).doubleValue();
    }
}
