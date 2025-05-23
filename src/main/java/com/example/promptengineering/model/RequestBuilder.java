package com.example.promptengineering.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestBuilder {
    private String model;
    private List<Message> messages = new ArrayList<>();
    private Integer maxTokens = 16000;
    private Boolean stream = true;
    private Double temperature = 0.0;
    private String key;
    private String provider;
    private String url;
    private String sessionId;
    private String reasoningEffort = "medium";
    private String type = "";
    private String system = "";

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

        if (this.provider.equals("ANTHROPIC")) {
            if (this.system != null && !this.system.trim().isEmpty()) {
                request.put("system", system);
            }
        } else {
            if (this.system != null && !this.system.trim().isEmpty()) {
                Content systemContent = new Content();
                systemContent.setType("text");
                systemContent.setText(this.system);
                ArrayList<Content> systemContentList = new ArrayList<>();
                systemContentList.add(systemContent);
                Message systemMessage = new Message("system", systemContentList);
                this.messages.add(0, systemMessage);
            }
        }

        List<Map<String, Object>> messagesListDefault = new ArrayList<>();
        for (Message message : messages) {
            messagesListDefault.add(message.toMap(provider, type));
        }
        request.put("messages", messagesListDefault);
        request.put("model", model);
        request.put("stream", stream);

        if (this.provider.equals("OPENAI")) {
            if (this.model.contains("o4-mini")) {
                request.put("response_format", Map.of("type", "text"));
                request.put("reasoning_effort", this.reasoningEffort);
            } else {
                request.put("max_tokens", maxTokens);
                request.put("temperature", temperature);
            }
        } else if (this.provider.equals("ANTHROPIC")) {
            request.put("max_tokens", maxTokens);
            request.put("temperature", temperature);

        }

        return request;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

   

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReasoningEffort() {
        return reasoningEffort;
    }

    public void setReasoningEffort(String reasoningEffort) {
        this.reasoningEffort = reasoningEffort;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

}