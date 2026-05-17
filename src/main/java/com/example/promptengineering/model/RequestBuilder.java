package com.example.promptengineering.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RequestBuilder {
    private String model;
    private List<Message> messages = new ArrayList<>();
    private Integer maxTokens = 16000;
    private Boolean stream = true;
    private Double temperature = 0.0;
    private String key;
    private String provider;
    private String url;
    private Double top_p = 0.95;
    private double frequencyPenalty = 0.0;
    private double presencePenalty = 0.0;
    private String sessionId;
    private String reasoningEffort = "";
    private String type = "";
    private String system = "";
    private boolean useSharedKeys = false;
    private Long sharedKeyId;
    private ProviderStrategy providerStrategy;

    public void setProvider(String providerName) {
        this.provider = providerName;
        if ("ANTHROPIC".equals(providerName)) {
            this.providerStrategy = new AnthropicStrategy();
        } else if ("GEMINI".equalsIgnoreCase(providerName)) {
            this.providerStrategy = new GeminiStrategy();
        } else {
            this.providerStrategy = new OpenAIStrategy();
        }
    }

    public ProviderStrategy getProviderStrategy() {
        if (providerStrategy == null) {
            this.setProvider(this.provider);
        }
        return providerStrategy;
    }

    public RequestBuilder model(String model) {
        this.model = model;
        return this;
    }

    public RequestBuilder addMessage(Message message) {
        messages.add(message);
        return this;
    }

    public RequestBuilder maxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public RequestBuilder stream(boolean stream) {
        this.stream = stream;
        return this;
    }

    public RequestBuilder temperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public Map<String, Object> build() {
        return getProviderStrategy().buildRequest(this);
    }

    public int estimateTokenCount() {
        int totalTokens = 0;

        if (this.system != null && !this.system.trim().isEmpty()) {
            totalTokens += this.system.length() / 4;
        }

        for (Message message : messages) {
            if (message.getContent() == null || message.getContent().isEmpty()) {
                continue;
            }
            for (Content content : message.getContent()) {
                totalTokens += content.estimateTokens();
            }
        }

        return totalTokens;
    }
}
