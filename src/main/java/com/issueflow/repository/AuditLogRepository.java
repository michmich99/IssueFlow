package com.issueflow.repository;

import com.issueflow.entity.AuditLog;
import com.issueflow.entity.enums.AuditAction;
import com.issueflow.entity.enums.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);
    List<AuditLog> findByEntityType(EntityType entityType);
    List<AuditLog> findByAction(AuditAction action);
    List<AuditLog> findByPerformedBy(Long userId);
}
