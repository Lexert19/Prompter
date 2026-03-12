package com.example.promptengineering.model;

import java.util.Map;

class CacheControl {
    public Map<String, Object> toMap() {
        String type = "ephemeral";
        return Map.of("type", type);
    }
}