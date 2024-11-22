package com.example.promptengineering.model;

import java.util.Map;

class CacheControl {
    private String type = "ephemeral";


    public Map<String, Object> toMap() {
        return Map.of("type", type);
    }
}