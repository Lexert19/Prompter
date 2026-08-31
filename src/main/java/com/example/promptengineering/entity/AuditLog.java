package com.example.promptengineering.entity;

import com.example.promptengineering.model.ActionType;
import com.example.promptengineering.model.ResultType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
@Getter @Setter
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;
  private String username;

  private String ipAddress;
  private String userAgent;
  private String sessionId;

  @Enumerated(EnumType.STRING)
  private ActionType action;

  @Enumerated(EnumType.STRING)
  private ResultType result;

  private String target;
  private String details;

  @Column(columnDefinition = "TEXT")
  private String payload;

  private LocalDateTime timestamp;

}