package com.example.promptengineering.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Service
public class ChatSessionRegistry {

    private final ConcurrentMap<String, ChatSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner;
    private final Duration ttl;

    public ChatSessionRegistry(
            @Value("${app.session.chat.ttl-minutes:120}") long ttlMinutes) {
        this.ttl = Duration.ofMinutes(ttlMinutes);
        this.cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "chat-session-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(this::evict, 5, 5, TimeUnit.MINUTES);
    }

    public ChatSession getOrCreate(UUID chatUuid, Long userId) {
        return sessions.computeIfAbsent(key(chatUuid, userId),
                k -> new ChatSession(chatUuid, userId));
    }

    public Optional<ChatSession> get(UUID chatUuid, Long userId) {
        return Optional.ofNullable(sessions.get(key(chatUuid, userId)));
    }

    public void remove(UUID chatUuid, Long userId) {
        sessions.remove(key(chatUuid, userId));
    }

    private void evict() {
        Instant cutoff = Instant.now().minus(ttl);
        int before = sessions.size();
        sessions.entrySet().removeIf(e -> e.getValue().getLastAccess().isBefore(cutoff));
        int n = before - sessions.size();
        if (n > 0)
            log.info("Evicted {} chat sessions", n);
    }

    private String key(UUID chatUuid, Long userId) {
        return userId + ":" + chatUuid;
    }

    @PreDestroy
    void shutdown() {
        cleaner.shutdownNow();
    }
}
