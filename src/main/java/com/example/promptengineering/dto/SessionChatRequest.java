package com.example.promptengineering.dto;

import java.util.List;
import java.util.Map;

public record SessionChatRequest(String text, String model, String provider,
        String providerStrategy, String url, String systemPrompt,
        List<String> enabledTools, Boolean useSharedKeys, List<String> documents,
        List<String> images, Long start, Double temperature, String reasoningEffort,
        Map<String, Object> providerConfig) {

    public SessionChatRequest(String text, String model, String provider,
            String providerStrategy, String url, String systemPrompt,
            List<String> enabledTools, Boolean useSharedKeys) {
        this(text, model, provider, providerStrategy, url, systemPrompt, enabledTools,
                useSharedKeys, List.of(), List.of(), null, null, null, null);
    }

    public SessionChatRequest(String text, String model, String provider,
            String providerStrategy, String url, String systemPrompt,
            List<String> enabledTools, Boolean useSharedKeys, List<String> documents,
            List<String> images, Long start) {
        this(text, model, provider, providerStrategy, url, systemPrompt, enabledTools,
                useSharedKeys, documents, images, start, null, null, null);
    }
}
