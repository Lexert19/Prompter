package com.example.promptengineering.restController;

import com.example.promptengineering.entity.AuditLog;
import com.example.promptengineering.model.ActionType;
import com.example.promptengineering.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuditController {

  private final AuditLogRepository auditLogRepository;

  @GetMapping("/logs")
  public ResponseEntity<Page<AuditLog>> getLogs(
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) ActionType action,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
      @RequestParam(required = false) String search,
      @PageableDefault(size = 50, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {

    Page<AuditLog> page = auditLogRepository.search(userId, action, from, to, search, pageable);
    return ResponseEntity.ok(page);
  }

  @GetMapping("/stats")
  public ResponseEntity<Map<String, Object>> getStats() {
    long total = auditLogRepository.count();
    return ResponseEntity.ok(Map.of("total", total));
  }
}