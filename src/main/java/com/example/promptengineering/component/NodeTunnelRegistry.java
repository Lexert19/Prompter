package com.example.promptengineering.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class NodeTunnelRegistry {
    private final ObjectMapper objectMapper;
    private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<String>> pending = new ConcurrentHashMap<>();

    public void register(UUID nodeId, WebSocketSession session) {
        sessions.put(nodeId, session);
    }

    public void unregister(UUID nodeId) {
        sessions.remove(nodeId);
    }

    public boolean isOnline(UUID nodeId) {
        return sessions.containsKey(nodeId);
    }

    public CompletableFuture<String> sendRequest(UUID nodeId,
                                                 Map<String, Object> payload) {
        WebSocketSession session = sessions.get(nodeId);
        if (session == null)
            throw new IllegalStateException("Node offline");

        String corrId = UUID.randomUUID().toString();
        payload.put("correlationId", corrId);

        CompletableFuture<String> future = new CompletableFuture<>();
        pending.put(corrId, future);

        try {
            String json = objectMapper.writeValueAsString(payload);
            session.send(Mono.just(session.textMessage(json))).subscribe(null, ex -> {
                pending.remove(corrId);
                future.completeExceptionally(ex);
            });
        } catch (JsonProcessingException e) {
            pending.remove(corrId);
            future.completeExceptionally(e);
        }
        return future.orTimeout(60, TimeUnit.SECONDS);
    }

    public void handleResponse(String corrId, String result) {
        CompletableFuture<String> f = pending.remove(corrId);
        if (f != null)
            f.complete(result);
    }
}
