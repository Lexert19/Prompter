package com.example.promptengineering.component;

import com.example.promptengineering.model.RateLimitPolicy;
import com.example.promptengineering.repository.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IpRateLimiter {
    private static final String KEY_PREFIX = "ip:";

    private final RateLimiter rateLimiter;
    private final RateLimitPolicy policy;

    public IpRateLimiter(RateLimiter rateLimiter,
            @Value("${rate.limit.max-requests}") int maxRequests,
            @Value("${rate.limit.time-window-seconds}") long timeWindowSeconds) {
        this.rateLimiter = rateLimiter;
        this.policy = new RateLimitPolicy(maxRequests, Duration.ZERO,
                Duration.ofSeconds(timeWindowSeconds));
    }

    public boolean isAllowed(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        return rateLimiter.tryAcquire(KEY_PREFIX + clientIp, policy);
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
}
