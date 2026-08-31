package com.example.promptengineering.service;

import com.example.promptengineering.entity.AuditLog;
import com.example.promptengineering.model.ActionType;
import com.example.promptengineering.model.ResultType;
import com.example.promptengineering.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;

  @Async("taskExecutor")
  public void logAsync(AuditLog auditLog) {
    try {
      auditLogRepository.save(auditLog);
    } catch (Exception e) {
      log.error("Failed to save audit log", e);
    }
  }

  public AuditLog createAuditLog(Long userId, String username, ActionType action,
      ResultType result, String target, String details, String payload) {
    AuditLog log = new AuditLog();
    log.setUserId(userId);
    log.setUsername(username);
    log.setAction(action);
    log.setResult(result);
    log.setTarget(target);
    log.setDetails(details);
    log.setPayload(payload);
    log.setTimestamp(LocalDateTime.now());

    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes != null) {
      HttpServletRequest request = attributes.getRequest();
      log.setIpAddress(getClientIp(request));
      log.setUserAgent(request.getHeader("User-Agent"));
      log.setSessionId(request.getSession().getId());
    }
    return log;
  }

  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    return ip != null ? ip.split(",")[0].trim() : null;
  }
}