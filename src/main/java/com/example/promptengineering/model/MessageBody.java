package com.example.promptengineering.model;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class MessageBody {
    private UUID chatUuid;
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
