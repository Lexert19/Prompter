package com.example.promptengineering.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeminiStrategy implements ProviderStrategy {
    @Override
    public void applySystemPrompt(Map<String, Object> request, List<Message> messages,
                                  String system) {
        if (system != null && !system.trim().isEmpty()) {
            request.put("system_instruction", system);
        }
    }

    @Override
    public Map<String, Object> formatTextContent(TextContent content, boolean cached) {
        return Map.of("type", "text", "text", content.getText());
    }

    @Override
    public Map<String, Object> formatImageContent(ImageContent content, boolean cached) {
        return Map.of("type", "image", "mime_type", content.getMediaType(), "data",
                content.getData());
    }

    @Override
    public Map<String, Object> buildRequest(RequestBuilder builder) {
        Map<String, Object> request = new HashMap<>();

        applySystemPrompt(request, builder.getMessages(), builder.getSystem());

        List<Map<String, Object>> steps = new ArrayList<>();
        for (Message message : builder.getMessages()) {
            String stepType = message.getRole().equalsIgnoreCase("assistant")
                ? "model_output"
                : "user_input";

            List<Map<String, Object>> contentParts = new ArrayList<>();
            for (Content content : message.getContent()) {
                contentParts.add(content.toMap(this, message.isCached()));
            }

            Map<String, Object> step = new HashMap<>();
            step.put("type", stepType);
            if (contentParts.size() == 1) {
                step.put("content", contentParts.get(0));
            } else {
                step.put("content", contentParts);
            }
            steps.add(step);
        }

        request.put("input", steps);

        request.put("store", false);

        Map<String, Object> genConfig = new HashMap<>();
        genConfig.put("temperature", builder.getTemperature());
        genConfig.put("max_output_tokens", builder.getMaxTokens());
        genConfig.put("top_p", builder.getTop_p());

        if (!builder.getReasoningEffort().isEmpty()) {
            genConfig.put("thinking_level", builder.getReasoningEffort().toLowerCase());
            genConfig.put("thinking_summaries", "auto");
        }
        request.put("generation_config", genConfig);
        request.put("model", builder.getModel());
        request.put("stream", builder.getStream());

        return request;
    }

}
