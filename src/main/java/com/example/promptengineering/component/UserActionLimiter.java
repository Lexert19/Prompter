package com.example.promptengineering.component;

import com.example.promptengineering.entity.User;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UserActionLimiter {
    private final ConcurrentMap<Long, AtomicInteger> attempts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Instant> lastAction = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Instant> blockedUntil = new ConcurrentHashMap<>();
    @Value("${app.rate-limit.user.max-attempts:3}")
    private int maxAttemptsPerDay;

    @Value("${app.rate-limit.user.cooldown-seconds:60}")
    private int cooldownSeconds;

    public boolean canPerform(User user) {
        if (user == null || user.getId() == null) return false;
        Long userId = user.getId();

        Instant blocked = blockedUntil.get(userId);
        if (blocked != null && Instant.now().isBefore(blocked)) return false;

        if (cooldownSeconds > 0) {
            Instant last = lastAction.get(userId);
            if (last != null && last.plusSeconds(cooldownSeconds).isAfter(Instant.now())) return false;
        }

        AtomicInteger count = attempts.computeIfAbsent(userId, k -> new AtomicInteger(0));
        int current = count.incrementAndGet();
        if (current > maxAttemptsPerDay) {
            blockedUntil.put(userId, Instant.now().plusSeconds(24 * 3600));
            attempts.remove(userId);
            lastAction.remove(userId);
            return false;
        }

        lastAction.put(userId, Instant.now());
        return true;
    }
}
