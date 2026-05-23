package com.issueflow.service;

import com.issueflow.dto.response.AuditLogResponse;
import com.issueflow.entity.AuditLog;
import com.issueflow.entity.enums.Actor;
import com.issueflow.entity.enums.AuditAction;
import com.issueflow.entity.enums.EntityType;
import com.issueflow.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepo;

    @InjectMocks
    private AuditLogService auditLogService;

    private AuditLog testLog;

    @BeforeEach
    void setUp() {
        testLog = new AuditLog();
        testLog.setId(1L);
        testLog.setAction(AuditAction.CREATE);
        testLog.setEntityType(EntityType.TICKET);
        testLog.setEntityId(100L);
        testLog.setPerformedBy(1L);
        testLog.setActor(Actor.USER);
        testLog.setTimestamp(LocalDateTime.now());
    }

    @Test
    void testLogAuditAction() {
        when(auditLogRepo.save(any(AuditLog.class))).thenReturn(testLog);

        auditLogService.log(AuditAction.CREATE, EntityType.TICKET, 100L, 1L, Actor.USER);

        verify(auditLogRepo, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testGetAuditLogsByEntityTypeAndId() {
        when(auditLogRepo.findByEntityTypeAndEntityId(EntityType.TICKET, 100L))
                .thenReturn(Arrays.asList(testLog));

        List<AuditLogResponse> logs = auditLogService.getAuditLogs(EntityType.TICKET, 100L, null, null);

        assertNotNull(logs);
        assertEquals(1, logs.size());
        assertEquals(EntityType.TICKET, logs.get(0).getEntityType());
        verify(auditLogRepo, times(1)).findByEntityTypeAndEntityId(EntityType.TICKET, 100L);
    }

    @Test
    void testGetAuditLogsByAction() {
        when(auditLogRepo.findByAction(AuditAction.CREATE))
                .thenReturn(Arrays.asList(testLog));

        List<AuditLogResponse> logs = auditLogService.getAuditLogs(null, null, AuditAction.CREATE, null);

        assertNotNull(logs);
        assertEquals(1, logs.size());
        assertEquals(AuditAction.CREATE, logs.get(0).getAction());
        verify(auditLogRepo, times(1)).findByAction(AuditAction.CREATE);
    }

    @Test
    void testGetAllAuditLogs() {
        when(auditLogRepo.findAll()).thenReturn(Arrays.asList(testLog));

        List<AuditLogResponse> logs = auditLogService.getAuditLogs(null, null, null, null);

        assertNotNull(logs);
        assertEquals(1, logs.size());
        verify(auditLogRepo, times(1)).findAll();
    }
}
