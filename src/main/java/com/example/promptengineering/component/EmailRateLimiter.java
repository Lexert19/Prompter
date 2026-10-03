package com.example.promptengineering.component;


import com.example.promptengineering.model.RateLimitPolicy;
import com.example.promptengineering.repository.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class EmailRateLimiter {

    private static final String KEY_PREFIX = "email:";
    private final RateLimiter rateLimiter;
    private final RateLimitPolicy policy;

    public EmailRateLimiter(
        RateLimiter rateLimiter,
        @Value("${app.rate-limit.email.max-attempts:3}") int maxAttempts,
        @Value("${app.rate-limit.email.cooldown-seconds:60}") int cooldownSeconds,
        @Value("${app.rate-limit.email.block-hours:24}") int blockHours) {
        this.rateLimiter = rateLimiter;
        this.policy = new RateLimitPolicy(
            maxAttempts,
            Duration.ofSeconds(cooldownSeconds),
            Duration.ofHours(blockHours)
        );
    }

    public boolean canSend(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return rateLimiter.tryAcquire(KEY_PREFIX + email.toLowerCase().trim(), policy);
    }

    public void reset(String email) {
        if (email != null) {
            rateLimiter.reset(KEY_PREFIX + email.toLowerCase().trim());
        }
    }
}