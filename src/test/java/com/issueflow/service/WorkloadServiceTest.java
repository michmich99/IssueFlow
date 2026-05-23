package com.issueflow.service;

import com.issueflow.dto.response.WorkloadResponse;
import com.issueflow.entity.Project;
import com.issueflow.entity.Ticket;
import com.issueflow.entity.User;
import com.issueflow.entity.enums.Priority;
import com.issueflow.entity.enums.Role;
import com.issueflow.entity.enums.TicketStatus;
import com.issueflow.entity.enums.TicketType;
import com.issueflow.repository.ProjectRepository;
import com.issueflow.repository.TicketRepository;
import com.issueflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceTest {

    @Mock
    private ProjectRepository projectRepo;

    @Mock
    private TicketRepository ticketRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private WorkloadService workloadService;

    private Project testProject;
    private User developer1;
    private User developer2;
    private List<Ticket> tickets;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");

        developer1 = new User();
        developer1.setId(1L);
        developer1.setUsername("dev1");
        developer1.setRole(Role.DEVELOPER);

        developer2 = new User();
        developer2.setId(2L);
        developer2.setUsername("dev2");
        developer2.setRole(Role.DEVELOPER);

        User admin = new User();
        admin.setId(3L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        Ticket ticket1 = new Ticket();
        ticket1.setId(1L);
        ticket1.setProject(testProject);
        ticket1.setAssignee(developer1);
        ticket1.setStatus(TicketStatus.TODO);
        ticket1.setPriority(Priority.HIGH);
        ticket1.setType(TicketType.BUG);

        Ticket ticket2 = new Ticket();
        ticket2.setId(2L);
        ticket2.setProject(testProject);
        ticket2.setAssignee(developer1);
        ticket2.setStatus(TicketStatus.IN_PROGRESS);
        ticket2.setPriority(Priority.MEDIUM);
        ticket2.setType(TicketType.FEATURE);

        Ticket ticket3 = new Ticket();
        ticket3.setId(3L);
        ticket3.setProject(testProject);
        ticket3.setAssignee(developer2);
        ticket3.setStatus(TicketStatus.TODO);
        ticket3.setPriority(Priority.LOW);
        ticket3.setType(TicketType.BUG);

        Ticket ticket4 = new Ticket();
        ticket4.setId(4L);
        ticket4.setProject(testProject);
        ticket4.setAssignee(developer1);
        ticket4.setStatus(TicketStatus.DONE);
        ticket4.setPriority(Priority.HIGH);
        ticket4.setType(TicketType.BUG);

        tickets = Arrays.asList(ticket1, ticket2, ticket3, ticket4);
        when(userRepo.findAll()).thenReturn(Arrays.asList(developer1, developer2, admin));
    }

    @Test
    void testGetProjectWorkload() {
        when(projectRepo.existsById(1L)).thenReturn(true);
        when(ticketRepo.findByProjectId(1L)).thenReturn(tickets);

        List<WorkloadResponse> workload = workloadService.getProjectWorkload(1L);

        assertNotNull(workload);
        assertEquals(2, workload.size());
        
        WorkloadResponse dev1Workload = workload.stream()
                .filter(w -> w.getUserId().equals(1L))
                .findFirst()
                .orElse(null);
        assertNotNull(dev1Workload);
        assertEquals(2L, dev1Workload.getOpenTicketCount());

        WorkloadResponse dev2Workload = workload.stream()
                .filter(w -> w.getUserId().equals(2L))
                .findFirst()
                .orElse(null);
        assertNotNull(dev2Workload);
        assertEquals(1L, dev2Workload.getOpenTicketCount());
    }

    @Test
    void testFindLeastLoadedDeveloper() {
        when(projectRepo.existsById(1L)).thenReturn(true);
        when(ticketRepo.findByProjectId(1L)).thenReturn(tickets);
        when(userRepo.findById(2L)).thenReturn(java.util.Optional.of(developer2));

        User leastLoaded = workloadService.findLeastLoadedDeveloper(1L);

        assertNotNull(leastLoaded);
        assertEquals(2L, leastLoaded.getId());
        assertEquals("dev2", leastLoaded.getUsername());
    }

    @Test
    void testFindLeastLoadedDeveloperNoDevs() {
        when(projectRepo.existsById(1L)).thenReturn(true);
        when(userRepo.findAll()).thenReturn(Arrays.asList());

        User leastLoaded = workloadService.findLeastLoadedDeveloper(1L);

        assertNull(leastLoaded);
    }
}
