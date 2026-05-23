package com.issueflow.service;

import com.issueflow.dto.response.ProjectResponse;
import com.issueflow.dto.response.TicketResponse;
import com.issueflow.entity.Project;
import com.issueflow.entity.Ticket;
import com.issueflow.entity.User;
import com.issueflow.entity.enums.Priority;
import com.issueflow.entity.enums.TicketStatus;
import com.issueflow.entity.enums.TicketType;
import com.issueflow.exception.ResourceNotFoundException;
import com.issueflow.repository.ProjectRepository;
import com.issueflow.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SoftDeleteServiceTest {

    @Mock
    private ProjectRepository projectRepo;

    @Mock
    private TicketRepository ticketRepo;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ProjectService projectService;

    @InjectMocks
    private TicketService ticketService;

    private Project testProject;
    private Ticket testTicket;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setOwner(testUser);
        testProject.setDeletedAt(null);

        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setTitle("Test Ticket");
        testTicket.setStatus(TicketStatus.TODO);
        testTicket.setPriority(Priority.MEDIUM);
        testTicket.setType(TicketType.BUG);
        testTicket.setProject(testProject);
        testTicket.setDeletedAt(null);
    }

    @Test
    void testSoftDeleteProject() {
        when(projectRepo.findActiveById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepo.save(any(Project.class))).thenReturn(testProject);

        projectService.deleteProject(1L);

        verify(projectRepo, times(1)).save(argThat(p -> p.getDeletedAt() != null));
        verify(auditLogService, times(1)).log(any(), any(), eq(1L), any(), any());
    }

    @Test
    void testGetDeletedProjects() {
        testProject.setDeletedAt(LocalDateTime.now());
        when(projectRepo.findAllDeleted()).thenReturn(Arrays.asList(testProject));

        List<ProjectResponse> deleted = projectService.getDeletedProjects();

        assertNotNull(deleted);
        assertEquals(1, deleted.size());
        verify(projectRepo, times(1)).findAllDeleted();
    }

    @Test
    void testRestoreProject() {
        testProject.setDeletedAt(LocalDateTime.now());
        when(projectRepo.findById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepo.save(any(Project.class))).thenReturn(testProject);

        projectService.restoreProject(1L);

        verify(projectRepo, times(1)).save(argThat(p -> p.getDeletedAt() == null));
        verify(auditLogService, times(1)).log(any(), any(), eq(1L), any(), any());
    }

    @Test
    void testRestoreProjectNotDeleted() {
        when(projectRepo.findById(1L)).thenReturn(Optional.of(testProject));

        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.restoreProject(1L);
        });

        verify(projectRepo, never()).save(any());
    }

    @Test
    void testSoftDeleteTicket() {
        when(ticketRepo.findActiveById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepo.save(any(Ticket.class))).thenReturn(testTicket);

        ticketService.deleteTicket(1L);

        verify(ticketRepo, times(1)).save(argThat(t -> t.getDeletedAt() != null));
        verify(auditLogService, times(1)).log(any(), any(), eq(1L), any(), any());
    }

    @Test
    void testRestoreTicket() {
        testTicket.setDeletedAt(LocalDateTime.now());
        when(ticketRepo.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepo.save(any(Ticket.class))).thenReturn(testTicket);

        ticketService.restoreTicket(1L);

        verify(ticketRepo, times(1)).save(argThat(t -> t.getDeletedAt() == null));
        verify(auditLogService, times(1)).log(any(), any(), eq(1L), any(), any());
    }
}
