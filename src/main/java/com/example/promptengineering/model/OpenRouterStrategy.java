package com.example.promptengineering.model;

import java.util.Map;

public class OpenRouterStrategy extends BaseOpenAICompatibleStrategy {

    @Override
    public Map<String, Object> buildRequest(RequestBuilder builder) {
        Map<String, Object> request = buildCommonRequest(builder);

        if (!builder.getReasoningEffort().isEmpty()) {
            request.put("reasoning_effort", builder.getReasoningEffort());
        }

        if (builder.getProviderConfig() != null) {
            request.put("provider", builder.getProviderConfig());
        }

        return request;
    }
}
