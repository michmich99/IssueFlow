package com.issueflow.service;

import com.issueflow.dto.response.AuditLogResponse;
import com.issueflow.entity.AuditLog;
import com.issueflow.entity.enums.Actor;
import com.issueflow.entity.enums.AuditAction;
import com.issueflow.entity.enums.EntityType;
import com.issueflow.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepo;

    @Transactional
    public void log(AuditAction action, EntityType entityType, Long entityId, Long performedBy, Actor actor) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setPerformedBy(performedBy);
        log.setActor(actor);
        auditLogRepo.save(log);
    }

    public List<AuditLogResponse> getAuditLogs(EntityType entityType, Long entityId, AuditAction action, Long performedBy) {
        List<AuditLog> logs;

        if (entityType != null && entityId != null) {
            logs = auditLogRepo.findByEntityTypeAndEntityId(entityType, entityId);
        } else if (entityType != null) {
            logs = auditLogRepo.findByEntityType(entityType);
        } else if (action != null) {
            logs = auditLogRepo.findByAction(action);
        } else if (performedBy != null) {
            logs = auditLogRepo.findByPerformedBy(performedBy);
        } else {
            logs = auditLogRepo.findAll();
        }

        return logs.stream()
            .map(AuditLogResponse::from)
            .collect(Collectors.toList());
    }
}
