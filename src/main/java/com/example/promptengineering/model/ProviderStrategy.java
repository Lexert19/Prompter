package com.example.promptengineering.model;

import java.util.List;
import java.util.Map;

public interface ProviderStrategy {
    void applySystemPrompt(Map<String, Object> request, List<Message> messages,
                           String system);
    Map<String, Object> formatTextContent(TextContent content, boolean cached);
    Map<String, Object> formatImageContent(ImageContent content, boolean cached);
    Map<String, Object> buildRequest(RequestBuilder builder);
}
