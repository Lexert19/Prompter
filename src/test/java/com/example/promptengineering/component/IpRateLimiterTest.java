package com.example.promptengineering.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class IpRateLimiterTest {

    @Autowired
    private IpRateLimiter limiter;

    @Value("${rate.limit.max-requests}")
    private int maxRequests;

    private static final String TEST_IP = "1.2.3.4";

    @BeforeEach
    void reset() {
        MockHttpServletRequest request = request(TEST_IP);
        limiter.isAllowed(request);
    }

    @Test
    void allowsUpToMaxRequests() {
        MockHttpServletRequest request = request(TEST_IP);

        for (int i = 1; i < maxRequests; i++) {
            assertTrue(limiter.isAllowed(request), "Request " + i + " should be allowed");
        }

        assertFalse(limiter.isAllowed(request),
                "Request " + (maxRequests + 1) + " should be blocked");
    }

    private MockHttpServletRequest request(String ip) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr(ip);
        return req;
    }
}
