package com.example.promptengineering.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnthropicStrategy implements ProviderStrategy {
    @Override
    public void applySystemPrompt(Map<String, Object> request, List<Message> messages, String system) {
        if (system != null && !system.trim().isEmpty()) {
            request.put("system", system);
        }
    }

    @Override
    public Map<String, Object> formatTextContent(TextContent content, boolean cached) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "text");
        map.put("text", content.getText());
        if (cached) {
            map.put("cache_control", new CacheControl().toMap());
        }
        return map;
    }

    @Override
    public Map<String, Object> formatImageContent(ImageContent content, boolean cached) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "image");
        map.put("source", Map.of("type", "base64", "media_type", content.getMediaType(), "data", content.getData()));
        if (cached) {
            map.put("cache_control", new CacheControl().toMap());
        }
        return map;
    }

    @Override
    public Map<String, Object> buildRequest(RequestBuilder builder) {
        Map<String, Object> request = new HashMap<>();
        applySystemPrompt(request, builder.getMessages(), builder.getSystem());

        List<Map<String, Object>> messagesList = new ArrayList<>();
        for (Message message : builder.getMessages()) {
            List<Map<String, Object>> contentList = new ArrayList<>();
            for (Content content : message.getContent()) {
                contentList.add(content.toMap(this, message.isCached()));
            }
            messagesList.add(Map.of("role", message.getRole(), "content", contentList));
        }

        request.put("messages", messagesList);
        request.put("model", builder.getModel());
        request.put("stream", builder.getStream());
        request.put("max_tokens", builder.getMaxTokens());
        request.put("temperature", builder.getTemperature());

        return request;
    }
}
