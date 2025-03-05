package com.example.promptengineering.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



//@Table("message_entity")
public class Message {
    // @PrimaryKey
    // @CassandraType(type = Name.BIGINT)
    private Long id;
    private String role;
    // @CassandraType(type = CassandraType.Name.TEXT)
    // @jakarta.persistence.Convert(converter = ContentListConverter.class)
    private List<Content> content;
    private String model;
    private String userId;
    private String sessionId;
    private String provider;
    private boolean cached;


    public Message(String role, List<Content> content) {
        this.role = role;
        this.content = content;
    }

    public Map<String, Object> toMap(String provider, String type) {
        switch (type.toLowerCase()) {
            case "text" -> {
                StringBuilder contentText = new StringBuilder();
                for (Content item : content) {
                    contentText.append(item.getText());
                }
                return Map.of(
                        "role", role,
                        "content", contentText.toString().trim());
            }
            default -> {
                List<Map<String, Object>> contentList = new ArrayList<>();
                for (Content item : content) {
                    contentList.add(item.toMap(provider, cached));
                }
                return Map.of(
                        "role", role,
                        "content", contentList);
            }
        }

    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Content> getContent() {
        return content;
    }

    public void setContent(List<Content> content) {
        this.content = content;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
