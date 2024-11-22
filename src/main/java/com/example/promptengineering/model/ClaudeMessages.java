package com.example.promptengineering.model;

import java.util.List;

import lombok.Data;

@Data
public class ClaudeMessages{
    private final String role;
    private List<Object> content;

    public ClaudeMessages(){
        this.role = "user";
    }
}
