package com.example.promptengineering.model;

import java.util.List;

import lombok.Data;

@Data
public class ClaudeBody {
    private final String model;
    private final int max_tokens;
    private List<Object> messages;
    private boolean stream;

    public ClaudeBody() {
        this.max_tokens = 1024;
        this.model = "claude-3-5-sonnet-20240620";
    }

 
}
