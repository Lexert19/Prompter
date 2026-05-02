package com.example.promptengineering.dto;

import com.example.promptengineering.entity.Chat;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class ChatDto {
    private UUID uuid;
    private Long userId;
    private String userEmail;

    public ChatDto() {
    }

    public static ChatDto fromEntity(Chat chat) {
        if (chat == null)
            return null;
        ChatDto dto = new ChatDto();
        dto.setUuid(chat.getUuid());
        if (chat.getUser() != null) {
            dto.setUserId(chat.getUser().getId());
            dto.setUserEmail(chat.getUser().getEmail());
        }
        return dto;
    }

    public static List<ChatDto> fromEntities(List<Chat> chats) {
        return chats.stream().map(ChatDto::fromEntity).collect(Collectors.toList());
    }

}
