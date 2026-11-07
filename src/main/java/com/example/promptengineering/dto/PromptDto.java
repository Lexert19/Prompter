package com.example.promptengineering.dto;

public record PromptDto(String id, String text, long createdAt, boolean truncated) {
}
