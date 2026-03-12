package com.example.promptengineering.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class MessageBody {
    private Long chatId;
    private String text;
    private List<String> documents;
    private List<String> images;
    private long start;
    private long end;
    private String role;
    private boolean cache;

    public boolean getCache() {
        return cache;
    }
}
