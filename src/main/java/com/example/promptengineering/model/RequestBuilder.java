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
            if (message.getContent() == null || message.getContent().isEmpty()) {
                Content defaultContent = new Content();
                defaultContent.setType("text");
                defaultContent.setText("error");
                message.setContent(List.of(defaultContent));
            }
            messagesListDefault.add(message.toMap(provider, type));
        }
        request.put("messages", messagesListDefault);
        request.put("model", model);
        request.put("stream", stream);

        if(!this.reasoningEffort.isEmpty()){
            request.put("response_format", Map.of("type", "text"));
            request.put("reasoning_effort", this.reasoningEffort);
        }
        request.put("max_tokens", maxTokens);
        request.put("temperature", temperature);
        request.put("top_p", top_p);


        return request;
    }
}