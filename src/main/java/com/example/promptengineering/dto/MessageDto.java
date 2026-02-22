package com.example.promptengineering.dto;

import com.example.promptengineering.entity.Message;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class MessageDto {
    private Long id;
    private String text;
    private List<String> documents;
    private List<String> images;
    private Instant createdAt;
    private long start;
    private long end;
    private String role;
    private boolean cache;
    private Long chatId;
    private String chatName;

    public MessageDto() {}

    public static MessageDto fromEntity(Message message) {
        if (message == null) return null;
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setText(message.getText());
        dto.setDocuments(message.getDocuments());
        dto.setImages(message.getImages());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setStart(message.getStart());
        dto.setEnd(message.getEnd());
        dto.setRole(message.getRole());
        dto.setCache(message.isCache());
        if (message.getChat() != null) {
            dto.setChatId(message.getChat().getId());
        }
        return dto;
    }

    public static List<MessageDto> fromEntities(List<Message> messages) {
        return messages.stream().map(MessageDto::fromEntity).collect(Collectors.toList());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public List<String> getDocuments() { return documents; }
    public void setDocuments(List<String> documents) { this.documents = documents; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public long getStart() { return start; }
    public void setStart(long start) { this.start = start; }
    public long getEnd() { return end; }
    public void setEnd(long end) { this.end = end; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isCache() { return cache; }
    public void setCache(boolean cache) { this.cache = cache; }
    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
    public String getChatName() { return chatName; }
    public void setChatName(String chatName) { this.chatName = chatName; }
}
