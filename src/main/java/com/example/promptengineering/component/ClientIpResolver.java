package com.example.promptengineering.component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ClientIpResolver {

    public String resolve(HttpServletRequest request) {
        String ip = extractFirstValid(request.getHeader("X-Real-IP"));
        if (ip == null) {
            ip = extractFirstValid(request.getHeader("X-Forwarded-For"));
        }
        if (ip == null) {
            ip = extractFirstValid(request.getHeader("Proxy-Client-IP"));
        }
        if (ip == null) {
            ip = extractFirstValid(request.getHeader("WL-Proxy-Client-IP"));
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String extractFirstValid(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        for (String part : header.split(",")) {
            String candidate = part.trim();
            if (!candidate.isEmpty() && !"unknown".equalsIgnoreCase(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}