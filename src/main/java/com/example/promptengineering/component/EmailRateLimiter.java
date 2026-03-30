package com.example.promptengineering.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class EmailRateLimiter {
    private final ConcurrentMap<String, AtomicInteger> attempts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Instant> lastSent = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Instant> blockedUntil = new ConcurrentHashMap<>();

    private final int maxAttemptsPerDay;
    private final int cooldownSeconds;

    public EmailRateLimiter(@Value("${app.rate-limit.email.max-attempts:3}") int maxAttemptsPerDay,
            @Value("${app.rate-limit.email.cooldown-seconds:60}") int cooldownSeconds) {
        this.maxAttemptsPerDay = maxAttemptsPerDay;
        this.cooldownSeconds = cooldownSeconds;
    }

    public boolean canSend(String email) {
        Instant blocked = blockedUntil.get(email);
        if (blocked != null && Instant.now().isBefore(blocked)) {
            return false;
        }
        Instant last = lastSent.get(email);
        if (last != null && last.plusSeconds(cooldownSeconds).isAfter(Instant.now())) {
            return false;
        }
        AtomicInteger count = attempts.computeIfAbsent(email, k -> new AtomicInteger(0));
        int current = count.incrementAndGet();
        if (current > maxAttemptsPerDay) {
            blockedUntil.put(email, Instant.now().plusSeconds(24 * 3600));
            attempts.remove(email);
            lastSent.remove(email);
            return false;
        }
        lastSent.put(email, Instant.now());
        return true;
    }

    public void reset(String email) {
        attempts.remove(email);
        lastSent.remove(email);
        blockedUntil.remove(email);
    }
}
