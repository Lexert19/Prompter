package com.example.promptengineering.component;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.RateLimitPolicy;
import com.example.promptengineering.repository.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class UserActionLimiter {

    private static final String KEY_PREFIX = "user:";
    private final RateLimiter rateLimiter;
    private final RateLimitPolicy policy;

    public UserActionLimiter(
        RateLimiter rateLimiter,
        @Value("${app.rate-limit.user.max-attempts:3}") int maxAttempts,
        @Value("${app.rate-limit.user.cooldown-seconds:60}") int cooldownSeconds,
        @Value("${app.rate-limit.user.block-hours:24}") int blockHours) {
        this.rateLimiter = rateLimiter;
        this.policy = new RateLimitPolicy(
            maxAttempts,
            Duration.ofSeconds(cooldownSeconds),
            Duration.ofHours(blockHours)
        );
    }

    public boolean canPerform(User user) {
        if (user == null || user.getId() == null) {
            return false;
        }
        return rateLimiter.tryAcquire(KEY_PREFIX + user.getId(), policy);
    }

    public void reset(User user) {
        if (user != null && user.getId() != null) {
            rateLimiter.reset(KEY_PREFIX + user.getId());
        }
    }
}