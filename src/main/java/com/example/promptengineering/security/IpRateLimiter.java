package com.example.promptengineering.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IpRateLimiter {

    @Value("${rate.limit.max-requests}")
    private int maxRequests;

    @Value("${rate.limit.time-window-seconds}")
    private long timeWindowSeconds;

    private final ConcurrentHashMap<String, RequestCounter> counters = new ConcurrentHashMap<>();

    public boolean isAllowed(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        RequestCounter counter = counters.computeIfAbsent(clientIp, k -> new RequestCounter());
        return counter.record();
    }

    private String getClientIp(HttpServletRequest request) {
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private class RequestCounter {
        private int count;
        private Instant windowStart = Instant.now();

        synchronized boolean record() {
            Instant now = Instant.now();
            if (now.isAfter(windowStart.plusSeconds(timeWindowSeconds))) {
                windowStart = now;
                count = 1;
                return true;
            } else {
                if (count < maxRequests) {
                    count++;
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
}