package com.example.promptengineering.model;

import java.util.Map;

public record ToolDefinition(String name, String description,
        Map<String, Object> parameters) {

    public Map<String, Object> toOpenAiFormat() {
        return Map.of("type", "function", "function", Map.of("name", name, "description",
                description, "parameters", parameters));
    }
}
