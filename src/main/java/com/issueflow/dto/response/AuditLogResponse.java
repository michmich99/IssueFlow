package com.issueflow.dto.response;

import com.issueflow.entity.AuditLog;
import com.issueflow.entity.enums.Actor;
import com.issueflow.entity.enums.AuditAction;
import com.issueflow.entity.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private AuditAction action;
    private EntityType entityType;
    private Long entityId;
    private Long performedBy;
    private Actor actor;
    private LocalDateTime timestamp;

    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
            log.getId(),
            log.getAction(),
            log.getEntityType(),
            log.getEntityId(),
            log.getPerformedBy(),
            log.getActor(),
            log.getTimestamp()
        );
    }
}
