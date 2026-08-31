package com.example.promptengineering.aspect;

import com.example.promptengineering.annotation.Auditable;
import com.example.promptengineering.entity.AuditLog;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.ResultType;
import com.example.promptengineering.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

  private final AuditLogService auditLogService;

  @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
  public void logSuccess(JoinPoint joinPoint, Auditable auditable, Object result) {
    AuditLog log = buildAuditLog(joinPoint, auditable, ResultType.SUCCESS);
    auditLogService.logAsync(log);
  }

  @AfterThrowing(pointcut = "@annotation(auditable)", throwing = "ex")
  public void logFailure(JoinPoint joinPoint, Auditable auditable, Exception ex) {
    AuditLog log = buildAuditLog(joinPoint, auditable, ResultType.FAILURE);
    log.setDetails(log.getDetails() + " | Exception: " + ex.getMessage());
    auditLogService.logAsync(log);
  }

  private AuditLog buildAuditLog(JoinPoint joinPoint, Auditable auditable, ResultType result) {
    User user = getCurrentUser();
    Long userId = user != null ? user.getId() : null;
    String username = user != null ? user.getEmail() : "anonymous";

    String target = auditable.target();
    String details = auditable.details();

    return auditLogService.createAuditLog(
        userId, username, auditable.action(), result,
        target, details, null
    );
  }

  private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof User) {
      return (User) auth.getPrincipal();
    }
    return null;
  }
}