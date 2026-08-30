package com.example.promptengineering.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestBuilder {
    private String model;
    private List<Message> messages = new ArrayList<>();
    private Integer maxTokens = 16000;
    private Boolean stream = true;
    private Double temperature = 0.0;
    private String key;
    @JsonProperty("provider")
    private String provider;
    @JsonProperty("providerStrategy")
    private Strategy compatibility = Strategy.OPENAI;
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
    @JsonIgnore
    private ProviderStrategy providerStrategy;
    private UUID communityNodeId;
    private Map<String, Object> providerConfig;

    private static final Map<Strategy, Supplier<ProviderStrategy>> STRATEGIES = Map.of(
        Strategy.ANTHROPIC, AnthropicStrategy::new,
        Strategy.GEMINI, GeminiStrategy::new,
        Strategy.OPENROUTER, OpenRouterStrategy::new,
        Strategy.OPENAI, OpenAIStrategy::new,
        Strategy.NVIDIA, OpenAIStrategy::new,
        Strategy.DEFAULT, OpenAIStrategy::new
    );

    public Map<String, Object> build() {
        return getProviderStrategy().buildRequest(this);
    }

    public void setProvider(String providerName) {
        this.provider = providerName.toUpperCase();
        if (this.providerStrategy == null) {
            this.compatibility = Strategy.fromString(providerName);
            this.providerStrategy = STRATEGIES.getOrDefault(this.compatibility, OpenAIStrategy::new).get();
        }
    }

    public RequestBuilder communityNode(UUID nodeId) {
        this.communityNodeId = nodeId;
        return this;
    }

    public void setProviderStrategy(String strategyName) {
        this.compatibility = Strategy.fromString(strategyName);
        this.providerStrategy = STRATEGIES.getOrDefault(this.compatibility, OpenAIStrategy::new).get();
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

    public ProviderStrategy getProviderStrategy() {
        if (providerStrategy == null) {
            Strategy type = compatibility != null ? compatibility : Strategy.fromString(provider);
            providerStrategy = STRATEGIES.getOrDefault(type, OpenAIStrategy::new).get();
        }
        return providerStrategy;
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
