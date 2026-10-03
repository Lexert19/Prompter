package com.example.promptengineering.component;

import com.example.promptengineering.model.RateLimitPolicy;
import com.example.promptengineering.repository.RateLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ConditionalOnProperty(name = "app.rate-limiter.type", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryRateLimiter implements RateLimiter {

    private record State(int attempts, Instant lastAction, Instant blockedUntil) {
    }

    private final ConcurrentMap<String, State> states = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key, RateLimitPolicy policy) {
        AtomicBoolean allowed = new AtomicBoolean(false);
        Instant now = Instant.now();

        states.compute(key, (k, state) -> {
            if (state == null) {
                allowed.set(true);
                return new State(1, now, null);
            }

            if (state.blockedUntil != null) {
                if (now.isBefore(state.blockedUntil)) {
                    allowed.set(false);
                    return state;
                }
                allowed.set(true);
                return new State(1, now, null);
            }

            if (policy.cooldown() != null && !policy.cooldown().isZero()
                    && state.lastAction != null) {
                if (now.isBefore(state.lastAction.plus(policy.cooldown()))) {
                    allowed.set(false);
                    return state;
                }
            }

            int currentAttempts = state.attempts;
            if (state.lastAction != null
                    && now.isAfter(state.lastAction.plus(policy.blockDuration()))) {
                currentAttempts = 0;
            }

            int newAttempts = currentAttempts + 1;
            if (newAttempts > policy.maxAttempts()) {
                allowed.set(false);
                return new State(0, null, now.plus(policy.blockDuration()));
            }

            allowed.set(true);
            return new State(newAttempts, now, null);
        });

        return allowed.get();
    }

    @Override
    public void reset(String key) {
        states.remove(key);
    }
}
