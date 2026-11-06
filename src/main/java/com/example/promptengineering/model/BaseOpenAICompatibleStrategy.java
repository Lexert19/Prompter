package com.example.promptengineering.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseOpenAICompatibleStrategy implements ProviderStrategy {

    @Override
    public void applySystemPrompt(Map<String, Object> request, List<Message> messages,
                                  String system) {
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
        return Map.of("type", "image_url", "image_url", Map.of("url",
                "data:" + content.getMediaType() + ";base64," + content.getData()));
    }

    protected Map<String, Object> buildCommonRequest(RequestBuilder builder) {
        Map<String, Object> request = new HashMap<>();

        applySystemPrompt(request, builder.getMessages(), builder.getSystem());

        List<Map<String, Object>> messagesList = new ArrayList<>();
        for (Message message : builder.getMessages()) {
            Map<String, Object> msg = new java.util.HashMap<>();
            msg.put("role", message.getRole());

            if (message.getToolCalls() != null && !message.getToolCalls().isEmpty()) {
                msg.put("tool_calls", message
                        .getToolCalls().stream().map(tc -> Map.of("id", tc.getId(),
                                "type", "function", "function", Map.of("name",
                                        tc.getName(), "arguments", tc.getArguments())))
                        .toList());

                List<Map<String, Object>> contentList = new ArrayList<>();
                for (Content c : message.getContent())
                    contentList.add(c.toMap(this, message.isCached()));
                msg.put("content", contentList.isEmpty() ? null : contentList);

            } else if ("tool".equals(message.getRole())) {
                msg.put("tool_call_id", message.getToolCallId());
                msg.put("content", message.getStringContent());

            } else {
                List<Map<String, Object>> contentList = new ArrayList<>();
                for (Content c : message.getContent())
                    contentList.add(c.toMap(this, message.isCached()));
                msg.put("content", contentList);
            }
            messagesList.add(msg);
        }

        request.put("messages", messagesList);
        request.put("model", builder.getModel());
        request.put("stream", builder.getStream());
        if (builder.isIncludeAdvancedParams()) {
            request.put("max_tokens", builder.getMaxTokens());
            request.put("temperature", builder.getTemperature());
            request.put("top_p", builder.getTop_p());
            request.put("frequency_penalty", builder.getFrequencyPenalty());
            request.put("presence_penalty", builder.getPresencePenalty());
        }

        if (builder.getTools() != null && !builder.getTools().isEmpty()) {
            request.put("tools", builder.getTools());
            request.put("tool_choice", "auto");
        }
        return request;
    }
}
