package com.example.promptengineering.model;

import java.util.Map;

public record ToolDefinition(String name, String description,
        Map<String, Object> parameters) {

    public Map<String, Object> toOpenAiFormat() {
        if (name.startsWith("openrouter:")) {
            return Map.of("type", name);
        }
        return Map.of("type", "function", "function", Map.of(
                "name", name, "description", description, "parameters", parameters));
    }
}
