package com.example.promptengineering.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenAIStrategy implements ProviderStrategy {
    @Override
    public void applySystemPrompt(Map<String, Object> request, List<Message> messages, String system) {
        if (system != null && !system.trim().isEmpty()) {
            TextContent systemContent = new TextContent();
            systemContent.setType("text");
            systemContent.setText(system);

            Message systemMessage = new Message("system", List.of(systemContent));
            messages.add(0, systemMessage);
        }
    }

    @Override
    public Map<String, Object> formatTextContent(TextContent content, boolean cached) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "text");
        map.put("text", content.getText());
        return map;
    }

    @Override
    public Map<String, Object> formatImageContent(ImageContent content, boolean cached) {
        return Map.of("type", "image_url", "image_url",
                Map.of("url", "data:" + content.getMediaType() + ";base64," + content.getData()));
    }
}
