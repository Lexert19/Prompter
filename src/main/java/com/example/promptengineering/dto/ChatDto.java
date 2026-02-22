package com.example.promptengineering.dto;

import com.example.promptengineering.entity.Chat;

import java.util.List;
import java.util.stream.Collectors;

public class ChatDto {
    private Long id;
    private Long userId;
    private String userEmail;

    public ChatDto() {}

    public static ChatDto fromEntity(Chat chat) {
        if (chat == null) return null;
        ChatDto dto = new ChatDto();
        dto.setId(chat.getId());
        if (chat.getUser() != null) {
            dto.setUserId(chat.getUser().getId());
            dto.setUserEmail(chat.getUser().getEmail());
        }
        return dto;
    }

    public static List<ChatDto> fromEntities(List<Chat> chats) {
        return chats.stream().map(ChatDto::fromEntity).collect(Collectors.toList());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}
