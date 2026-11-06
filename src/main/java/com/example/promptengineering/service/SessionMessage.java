package com.example.promptengineering.service;

import com.example.promptengineering.model.Content;
import com.example.promptengineering.model.Message;
import com.example.promptengineering.model.TextContent;
import com.example.promptengineering.model.ToolCall;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class SessionMessage {
    private final UUID uuid;
    private final Message providerMessage;
    private final List<String> documents;
    private final List<String> images;
    private final long start;
    private final long end;
    private final Instant createdAt;
    private final String reasoning;

    public SessionMessage(Message providerMessage, List<String> documents,
            List<String> images, long start, long end) {
        this(providerMessage, documents, images, start, end, UUID.randomUUID(),
                Instant.now(), null);
    }

    public SessionMessage(Message providerMessage, List<String> documents,
            List<String> images, long start, long end, UUID uuid, Instant createdAt,
            String reasoning) {
        this.providerMessage = providerMessage;
        this.documents = documents != null ? List.copyOf(documents) : List.of();
        this.images = images != null ? List.copyOf(images) : List.of();
        this.start = start;
        this.end = end;
        this.uuid = uuid;
        this.createdAt = createdAt;
        this.reasoning = reasoning;
    }

    public String getRole() {
        return providerMessage.getRole();
    }

    public boolean isCached() {
        return providerMessage.isCached();
    }

    public String getText() {
        if (providerMessage.getContent() == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (Content c : providerMessage.getContent()) {
            if (c instanceof TextContent tc && tc.getText() != null)
                sb.append(tc.getText());
        }
        return sb.toString();
    }

    public List<ToolCall> getToolCalls() {
        return providerMessage.getToolCalls();
    }

    public String getToolCallId() {
        return providerMessage.getToolCallId();
    }

    public String getStringContent() {
        return providerMessage.getStringContent();
    }
}
