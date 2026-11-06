package com.example.promptengineering.dto;

import java.util.List;
import java.util.UUID;

public record SessionStateResponse(UUID chatUuid, String model, String provider,
        String systemPrompt, List<String> enabledTools, int messageCount,
        boolean hasToolCallsPending) {
}
