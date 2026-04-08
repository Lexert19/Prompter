package com.example.promptengineering.model;

import java.util.ArrayList;
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
        request.put("top_p", builder.getTop_p());
        request.put("frequency_penalty", builder.getFrequencyPenalty());
        request.put("presence_penalty", builder.getPresencePenalty());

        if (!builder.getReasoningEffort().isEmpty()) {
            request.put("response_format", Map.of("type", "text"));
            request.put("reasoning_effort", builder.getReasoningEffort());
            request.put("chat_template_kwargs", Map.of("enable_thinking", true));
        }

        return request;
    }
}
