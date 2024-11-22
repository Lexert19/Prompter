package com.example.promptengineering.model;

import lombok.Data;

@Data
public class SaveMessage {
    private String id;
    private String content;
    private boolean cache;
}
