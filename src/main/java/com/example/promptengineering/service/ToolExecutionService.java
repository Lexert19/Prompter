package com.example.promptengineering.service;

import com.example.promptengineering.entity.Chat;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.Message;
import com.example.promptengineering.model.ToolCall;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ToolExecutionService {

    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;
    private final ChatSessionPersistenceService persistence;
    private final SessionMessageMapper messageMapper;
    private final SseEventFactory sse;

    public Flux<ServerSentEvent<String>> execute(ToolCall call, ChatSession session,
                                                 User user) {
        ServerSentEvent<String> callEvent = sse.event("tool_call", Map.of("id",
                call.getId(), "name", call.getName(), "arguments", call.getArguments()));

        Mono<ServerSentEvent<String>> resultEvent = Mono
                .fromCallable(() -> runToolAndEmit(call, session, user))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> Mono.just(emitFailure(call, session, e)));

        return Flux.just(callEvent).concatWith(resultEvent);
    }

    private ServerSentEvent<String> runToolAndEmit(ToolCall call, ChatSession session,
                                                   User user)
            throws Exception {
        ToolExecutor exec = toolRegistry.getExecutor(call.getName()).orElseThrow(
                () -> new IllegalStateException("Unknown tool: " + call.getName()));
        Map<String, Object> args = parseJson(call.getArguments());
        String result = exec.execute(args, user, session);

        persistToolMessage(session,
                messageMapper.toolMessage(call.getId(), call.getName(), result));
        return sse.event("tool_result",
                Map.of("id", call.getId(), "name", call.getName(), "content", result));
    }

    private ServerSentEvent<String> emitFailure(ToolCall call, ChatSession session,
                                                Throwable e) {
        String err = "Tool '" + call.getName() + "' failed: " + e.getMessage();
        if (isExpectedClientError(e)) {
            log.debug("{} (expected client error, no stack trace)", err);
        } else {
            log.warn(err, e);
        }
        persistToolMessage(session,
                messageMapper.toolMessage(call.getId(), call.getName(), err));
        return sse.event("tool_error",
                Map.of("id", call.getId(), "name", call.getName(), "error", err));
    }

    private void persistToolMessage(ChatSession session, Message message) {
        SessionMessage toolMessage = session.addToolMessage(message);
        Chat chat = session.getChatEntity();
        if (chat == null) {
            log.warn("Cannot persist tool message: session {} not hydrated (chatUuid={})",
                    session.getChatUuid(), toolMessage.getUuid());
            return;
        }
        try {
            persistence.save(chat, toolMessage);
        } catch (Exception e) {
            log.error("Failed to persist tool message {} for chat {}",
                    toolMessage.getUuid(), session.getChatUuid(), e);
        }
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

    private static boolean isExpectedClientError(Throwable e) {
        if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException w) {
            return w.getStatusCode().is4xxClientError();
        }
        return e instanceof IllegalArgumentException
                || e instanceof IllegalStateException;
    }
}
