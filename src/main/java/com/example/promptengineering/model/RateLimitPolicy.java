package com.example.promptengineering.model;

import java.time.Duration;

public record RateLimitPolicy(
    int maxAttempts,
    Duration cooldown,
    Duration blockDuration
) {}