package com.example.promptengineering.service;

import com.example.promptengineering.entity.Chat;
import com.example.promptengineering.model.Message;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@Setter
public class ChatSession {

    private final UUID chatUuid;
    private final Long userId;
    private final Instant createdAt = Instant.now();
    private final List<SessionMessage> messages = new CopyOnWriteArrayList<>();
    private final Set<String> enabledTools = ConcurrentHashMap.newKeySet();
    private final AtomicReference<Instant> lastAccess = new AtomicReference<>(
            Instant.now());

    private volatile String model;
    private volatile String provider;
    private volatile String providerStrategy;
    private volatile String systemPrompt;
    private volatile String url;
    private volatile boolean useSharedKeys;
    private Chat chatEntity;
    private boolean hydrated = false;

    private volatile Double temperature;
    private volatile Integer maxTokens;
    private volatile Double topP;
    private volatile Double frequencyPenalty;
    private volatile Double presencePenalty;
    private volatile String reasoningEffort;
    private volatile Map<String, Object> providerConfig;

    public ChatSession(UUID chatUuid, Long userId) {
        this.chatUuid = chatUuid;
        this.userId = userId;
    }

    public void touch() {
        lastAccess.set(Instant.now());
    }

    public SessionMessage addUserMessage(Message m, List<String> documents,
                                         List<String> images, long start) {
        SessionMessage sm = new SessionMessage(m, documents, images, start, start);
        messages.add(sm);
        touch();
        return sm;
    }

    public SessionMessage addAssistantMessage(Message m, String reasoning, long start,
                                              long end) {
        SessionMessage sm = new SessionMessage(m, List.of(), List.of(), start, end,
                UUID.randomUUID(), Instant.now(), reasoning);
        messages.add(sm);
        touch();
        return sm;
    }

    public SessionMessage addToolMessage(Message m) {
        long now = System.currentTimeMillis();
        SessionMessage sm = new SessionMessage(m, List.of(), List.of(), now, now);
        messages.add(sm);
        touch();
        return sm;
    }

    public void setEnabledTools(Set<String> tools) {
        enabledTools.clear();
        if (tools != null)
            enabledTools.addAll(tools);
    }

    public Instant getLastAccess() {
        return lastAccess.get();
    }
}
