package com.example.promptengineering.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
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
        } else {
            this.providerStrategy = new OpenAIStrategy();
        }
    }

    public ProviderStrategy getProviderStrategy(){
        if(providerStrategy == null){
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
        Map<String, Object> request = new HashMap<>();

        getProviderStrategy().applySystemPrompt(request, this.messages, this.system);

        List<Map<String, Object>> messagesListDefault = new ArrayList<>();
        for (Message message : messages) {
            List<Map<String, Object>> contentList = new ArrayList<>();
            for (Content content : message.getContent()) {
                contentList.add(content.toMap(providerStrategy, message.isCached()));
            }

            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("role", message.getRole());
            msgMap.put("content", contentList);
            messagesListDefault.add(msgMap);
        }
        request.put("messages", messagesListDefault);
        request.put("model", model);
        request.put("stream", stream);

        if(!this.reasoningEffort.isEmpty()){
            request.put("response_format", Map.of("type", "text"));
            request.put("reasoning_effort", this.reasoningEffort);
            request.put("chat_template_kwargs", Map.of("enable_thinking", true));
        }
        request.put("max_tokens", maxTokens);
        request.put("temperature", temperature);
        request.put("top_p", top_p);


        return request;
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