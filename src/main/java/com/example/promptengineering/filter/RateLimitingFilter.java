package com.example.promptengineering.filter;

import com.example.promptengineering.security.IpRateLimiter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@Order(1)
public class RateLimitingFilter implements Filter {

    private final IpRateLimiter rateLimiter;

    public RateLimitingFilter(IpRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        if (path.startsWith("/auth/login") && httpRequest.getMethod().equalsIgnoreCase("POST")) {
            if (!rateLimiter.isAllowed(httpRequest)) {
                httpResponse.setStatus(429);
                httpResponse.getWriter().write("Too many requests. Please try again later.");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
