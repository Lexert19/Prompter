package com.example.promptengineering.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SseEventFactory {

    private final ObjectMapper objectMapper;

    public ServerSentEvent<String> event(String name, Map<String, ?> data) {
        try {
            return ServerSentEvent.<String>builder().event(name)
                    .data(objectMapper.writeValueAsString(data)).build();
        } catch (Exception e) {
            return ServerSentEvent.<String>builder().event("error")
                    .data("{\"error\":\"serialization\"}").build();
        }
    }

    public ServerSentEvent<String> error(String message) {
        return event("error", Map.of("error", message));
    }
}
