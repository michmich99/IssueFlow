package com.issueflow.controller;

import com.issueflow.dto.response.AuditLogResponse;
import com.issueflow.entity.enums.AuditAction;
import com.issueflow.entity.enums.EntityType;
import com.issueflow.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) EntityType entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) Long actor) {
        
        List<AuditLogResponse> logs = auditLogService.getAuditLogs(entityType, entityId, action, actor);
        return ResponseEntity.ok(logs);
    }
}
