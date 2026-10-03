package com.example.promptengineering.repository;

import com.example.promptengineering.model.RateLimitPolicy;

public interface RateLimiter {
  boolean tryAcquire(String key, RateLimitPolicy policy);

  void reset(String key);
}