package com.example.promptengineering.model;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private Long id;
    private String role;
    private List<Content> content;
    private String model;
    private String userId;
    private String sessionId;
    private String provider;
    private boolean cached;

    public Message(String role, List<Content> content) {
        this.role = role;
        setContent(content);
    }

    public void setContent(List<Content> content) {
        this.content = Objects.requireNonNullElseGet(content, ArrayList::new);
    }

}
