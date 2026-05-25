package com.example.promptengineering.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class WebSocketPublisher {

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void register(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public void unregister(String sessionId) {
        sessions.remove(sessionId);
    }

    public void publish(String sessionId, Object payload) {
        WebSocketSession session = sessions.get(sessionId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(payload);
            session.send(Mono.just(session.textMessage(json)));
        } catch (IOException e) {
            sessions.remove(sessionId);
        }
    }

    public void broadcast(Object payload) {
        sessions.keySet().forEach(id -> publish(id, payload));
    }
}
