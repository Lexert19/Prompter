package com.example.promptengineering.filter;

import com.example.promptengineering.entity.AuditLog;
import com.example.promptengineering.model.ActionType;
import com.example.promptengineering.model.ResultType;
import com.example.promptengineering.service.AuditLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Order(3)
@RequiredArgsConstructor
public class AuditFilter extends OncePerRequestFilter {

  private final AuditLogService auditLogService;

  private static final List<String> SENSITIVE_PATHS = Arrays.asList(
      "/api/admin/", "/api/users/", "/api/account/keys", "/api/2fa/"
  );

  private static final List<String> IGNORE_PATHS = Arrays.asList(
      "/api/admin/media/upload"
  );

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String path = request.getRequestURI();
    boolean isSensitive = SENSITIVE_PATHS.stream().anyMatch(path::startsWith) &&
        IGNORE_PATHS.stream().noneMatch(path::startsWith);

    String method = request.getMethod();
    boolean isWriteMethod = method.equals("GET") || method.equals("POST") ||
        method.equals("PUT") || method.equals("DELETE");

    if (isSensitive && isWriteMethod) {

    }

    filterChain.doFilter(request, response);
  }
}