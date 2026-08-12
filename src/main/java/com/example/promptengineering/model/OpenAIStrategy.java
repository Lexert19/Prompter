package com.example.promptengineering.model;

import java.util.Map;

public class OpenAIStrategy extends BaseOpenAICompatibleStrategy {
    @Override
    public Map<String, Object> buildRequest(RequestBuilder builder) {
        Map<String, Object> request = buildCommonRequest(builder);

        if (!builder.getReasoningEffort().isEmpty()) {
            request.put("response_format", Map.of("type", "text"));
            request.put("reasoning_effort", builder.getReasoningEffort());
            request.put("chat_template_kwargs", Map.of("enable_thinking", true));
        }

        return request;
    }
}
