package com.example.promptengineering.repository;

import com.example.promptengineering.entity.AuditLog;
import com.example.promptengineering.model.ActionType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
  Page<AuditLog> findByUserId(Long userId, Pageable pageable);
  Page<AuditLog> findByAction(ActionType action, Pageable pageable);
  Page<AuditLog> findByTimestampBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

  @Query("SELECT a FROM AuditLog a WHERE " +
      "(:userId IS NULL OR a.userId = :userId) AND " +
      "(:action IS NULL OR a.action = :action) AND " +
      "(:from IS NULL OR a.timestamp >= :from) AND " +
      "(:to IS NULL OR a.timestamp <= :to) AND " +
      "(:search IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.details) LIKE LOWER(CONCAT('%', :search, '%')))")
  Page<AuditLog> search(@Param("userId") Long userId,
      @Param("action") ActionType action,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      @Param("search") String search,
      Pageable pageable);

  long deleteByTimestampBefore(LocalDateTime cutoff);
  List<AuditLog> findByUserIdAndAction(Long userId, ActionType action);
}