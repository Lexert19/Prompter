package com.example.promptengineering.model;

import lombok.Data;

@Data
public class ClaudeSettings {
    private String apiKey;
    private final String version;
    private final String model;
    private final int maxTokens;

    public ClaudeSettings(String apiKey){
        this.version = "2023-06-01";
        this.maxTokens = 1024;
        this.model = "claude-3-5-sonnet-20240620";
        this.apiKey = apiKey;
    }
}
