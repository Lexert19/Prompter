package com.example.promptengineering.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
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
        if (content == null) {
            this.content = new ArrayList<>();
        } else {
            this.content = content;
        }
    }

    // public Map<String, Object> toMap(String provider, String type) {
    // switch (type.toLowerCase()) {
    // case "text" -> {
    // StringBuilder contentText = new StringBuilder();
    // for (Content item : content) {
    // contentText.append(item.getText());
    // }
    // return Map.of(
    // "role", role,
    // "content", contentText.toString().trim());
    // }
    // default -> {
    // List<Map<String, Object>> contentList = new ArrayList<>();
    // for (Content item : content) {
    // contentList.add(item.toMap(provider, cached));
    // }
    // return Map.of(
    // "role", role,
    // "content", contentList);
    // }
    // }
    //
    // }

}
