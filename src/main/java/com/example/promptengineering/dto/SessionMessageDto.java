package com.example.promptengineering.dto;

import java.util.List;

public record SessionMessageDto(String id, String role, String text, boolean cached,
        List<String> images, List<String> documents, long start, long end, long createdAt,
        List<ToolCallDto> toolCalls, String toolCallId, String toolName,
        String toolResult, String reasoning) {
}
