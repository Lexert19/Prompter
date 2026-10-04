package com.example.promptengineering.repository;

import com.example.promptengineering.entity.AuditLog;
import com.example.promptengineering.model.ActionType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>,
    JpaSpecificationExecutor<AuditLog> {
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);
    Page<AuditLog> findByAction(ActionType action, Pageable pageable);
    Page<AuditLog> findByTimestampBetween(LocalDateTime from, LocalDateTime to,
                                          Pageable pageable);


    long deleteByTimestampBefore(LocalDateTime cutoff);
    List<AuditLog> findByUserIdAndAction(Long userId, ActionType action);
}
