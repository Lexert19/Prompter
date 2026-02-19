package com.example.promptengineering.component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final String loginPath;

    public CustomAuthenticationEntryPoint(String loginPath) {
        this.loginPath = loginPath;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        String contextPath = request.getContextPath();
        if (request.getRequestURI().startsWith(contextPath + "/api")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        } else {
            String redirectUrl = contextPath + loginPath;
            response.sendRedirect(redirectUrl);
        }
    }
}