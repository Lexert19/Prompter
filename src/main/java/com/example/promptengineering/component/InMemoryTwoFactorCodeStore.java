package com.example.promptengineering.component;

import com.example.promptengineering.repository.TwoFactorCodeStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@ConditionalOnProperty(name = "app.2fa.store", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryTwoFactorCodeStore implements TwoFactorCodeStore {

    private record Entry(String code, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final ConcurrentMap<String, Entry> store = new ConcurrentHashMap<>();

    @Override
    public void save(String key, String code, Duration ttl) {
        store.put(key, new Entry(code, Instant.now().plus(ttl)));
    }

    @Override
    public Optional<String> get(String key) {
        Entry entry = store.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.isExpired()) {
            store.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.code());
    }

    @Override
    public void remove(String key) {
        store.remove(key);
    }
}
