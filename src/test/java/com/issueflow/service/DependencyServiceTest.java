package com.issueflow.service;

import com.issueflow.dto.response.DependencyTicketResponse;
import com.issueflow.entity.Project;
import com.issueflow.entity.Ticket;
import com.issueflow.entity.TicketDependency;
import com.issueflow.entity.enums.TicketStatus;
import com.issueflow.exception.BadRequestException;
import com.issueflow.exception.ResourceNotFoundException;
import com.issueflow.repository.TicketDependencyRepository;
import com.issueflow.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DependencyServiceTest {

    @Mock
    private TicketDependencyRepository dependencyRepo;

    @Mock
    private TicketRepository ticketRepo;

    @InjectMocks
    private DependencyService dependencyService;

    private Ticket testTicket;
    private Ticket blockerTicket;

    @BeforeEach
    void setUp() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setTitle("Test Ticket");
        testTicket.setStatus(TicketStatus.TODO);
        testTicket.setProject(project);

        blockerTicket = new Ticket();
        blockerTicket.setId(2L);
        blockerTicket.setTitle("Blocker Ticket");
        blockerTicket.setStatus(TicketStatus.IN_PROGRESS);
        blockerTicket.setProject(project);
    }

    @Test
    void testAddDependency() {
        when(ticketRepo.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepo.findById(2L)).thenReturn(Optional.of(blockerTicket));
        when(dependencyRepo.findByTicketIdAndBlockedById(1L, 2L)).thenReturn(Optional.empty());

        dependencyService.addDependency(1L, 2L);

        verify(dependencyRepo, times(1)).save(any(TicketDependency.class));
    }

    @Test
    void testAddDependencySelfBlock() {
        when(ticketRepo.findById(1L)).thenReturn(Optional.of(testTicket));

        assertThrows(BadRequestException.class, () -> {
            dependencyService.addDependency(1L, 1L);
        });

        verify(dependencyRepo, never()).save(any());
    }

    @Test
    void testAddDependencyAlreadyExists() {
        TicketDependency existing = new TicketDependency();
        existing.setTicket(testTicket);
        existing.setBlockedBy(blockerTicket);

        when(ticketRepo.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepo.findById(2L)).thenReturn(Optional.of(blockerTicket));
        when(dependencyRepo.findByTicketIdAndBlockedById(1L, 2L)).thenReturn(Optional.of(existing));

        assertThrows(BadRequestException.class, () -> {
            dependencyService.addDependency(1L, 2L);
        });

        verify(dependencyRepo, never()).save(any());
    }

    @Test
    void testGetDependencies() {
        TicketDependency dependency = new TicketDependency();
        dependency.setTicket(testTicket);
        dependency.setBlockedBy(blockerTicket);

        when(ticketRepo.existsById(1L)).thenReturn(true);
        when(dependencyRepo.findByTicketId(1L)).thenReturn(Arrays.asList(dependency));

        List<DependencyTicketResponse> dependencies = dependencyService.getDependencies(1L);

        assertNotNull(dependencies);
        assertEquals(1, dependencies.size());
        assertEquals(2L, dependencies.get(0).getId());
        assertEquals("Blocker Ticket", dependencies.get(0).getTitle());
    }

    @Test
    void testRemoveDependency() {
        TicketDependency dependency = new TicketDependency();
        dependency.setTicket(testTicket);
        dependency.setBlockedBy(blockerTicket);

        when(ticketRepo.existsById(1L)).thenReturn(true);
        when(dependencyRepo.findByTicketIdAndBlockedById(1L, 2L)).thenReturn(Optional.of(dependency));

        dependencyService.removeDependency(1L, 2L);

        verify(dependencyRepo, times(1)).deleteByTicketIdAndBlockedById(1L, 2L);
    }

    @Test
    void testRemoveDependencyNotFound() {
        when(ticketRepo.existsById(1L)).thenReturn(true);
        when(dependencyRepo.findByTicketIdAndBlockedById(1L, 999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            dependencyService.removeDependency(1L, 999L);
        });

        verify(dependencyRepo, never()).deleteByTicketIdAndBlockedById(any(), any());
    }
}
