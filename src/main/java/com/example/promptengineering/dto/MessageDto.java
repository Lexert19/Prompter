package com.example.promptengineering.dto;

import com.example.promptengineering.entity.Message;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
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
        dto.setCache(message.getCache());
        if (message.getChat() != null) {
            dto.setChatId(message.getChat().getId());
        }
        return dto;
    }

    public static List<MessageDto> fromEntities(List<Message> messages) {
        return messages.stream().map(MessageDto::fromEntity).collect(Collectors.toList());
    }

}
