package com.issueflow.service;

import com.issueflow.dto.request.CreateTicketRequest;
import com.issueflow.dto.request.UpdateTicketRequest;
import com.issueflow.entity.Project;
import com.issueflow.entity.Ticket;
import com.issueflow.entity.User;
import com.issueflow.entity.enums.Priority;
import com.issueflow.entity.enums.Role;
import com.issueflow.entity.enums.TicketStatus;
import com.issueflow.entity.enums.TicketType;
import com.issueflow.exception.BadRequestException;
import com.issueflow.exception.ResourceNotFoundException;
import com.issueflow.repository.ProjectRepository;
import com.issueflow.repository.TicketRepository;
import com.issueflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepo;

    @Mock
    private ProjectRepository projectRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private WorkloadService workloadService;

    @InjectMocks
    private TicketService ticketService;

    private User testUser;
    private Project testProject;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(Role.DEVELOPER);

        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setOwner(testUser);

        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setTitle("Test Ticket");
        testTicket.setStatus(TicketStatus.TODO);
        testTicket.setPriority(Priority.MEDIUM);
        testTicket.setType(TicketType.BUG);
        testTicket.setProject(testProject);
    }

    @Test
    void testCreateTicket() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("New Ticket");
        request.setProjectId(1L);
        request.setStatus(TicketStatus.TODO);
        request.setPriority(Priority.HIGH);
        request.setType(TicketType.FEATURE);
        request.setAssigneeId(1L);

        when(projectRepo.findActiveById(1L)).thenReturn(Optional.of(testProject));
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(ticketRepo.save(any(Ticket.class))).thenReturn(testTicket);

        var response = ticketService.createTicket(request);

        assertNotNull(response);
        verify(ticketRepo, times(1)).save(any(Ticket.class));
        verify(auditLogService, times(1)).log(any(), any(), any(), any(), any());
    }

    @Test
    void testUpdateTicketWithDoneStatus() {
        testTicket.setStatus(TicketStatus.DONE);
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setTitle("Updated Title");

        when(ticketRepo.findActiveById(1L)).thenReturn(Optional.of(testTicket));

        assertThrows(BadRequestException.class, () -> {
            ticketService.updateTicket(1L, request);
        });
    }

    @Test
    void testInvalidStatusTransition() {
        testTicket.setStatus(TicketStatus.TODO);
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setStatus(TicketStatus.DONE);

        when(ticketRepo.findActiveById(1L)).thenReturn(Optional.of(testTicket));

        assertThrows(BadRequestException.class, () -> {
            ticketService.updateTicket(1L, request);
        });
    }

    @Test
    void testValidStatusTransition() {
        testTicket.setStatus(TicketStatus.TODO);
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setStatus(TicketStatus.IN_PROGRESS);

        when(ticketRepo.findActiveById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepo.save(any(Ticket.class))).thenReturn(testTicket);

        var response = ticketService.updateTicket(1L, request);

        assertNotNull(response);
        verify(ticketRepo, times(1)).save(any(Ticket.class));
        verify(auditLogService, times(1)).log(any(), any(), any(), any(), any());
    }

    @Test
    void testGetTicketByIdNotFound() {
        when(ticketRepo.findActiveById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.getTicketById(999L);
        });
    }
}
