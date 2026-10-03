package com.example.promptengineering.repository;

import java.time.Duration;
import java.util.Optional;

public interface TwoFactorCodeStore {
    void save(String key, String code, Duration ttl);
    Optional<String> get(String key);
    void remove(String key);
}
