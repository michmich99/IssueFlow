package com.issueflow.controller;

import com.issueflow.dto.response.ProjectResponse;
import com.issueflow.dto.response.TicketResponse;
import com.issueflow.service.ProjectService;
import com.issueflow.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SoftDeleteController {

    private final ProjectService projectService;
    private final TicketService ticketService;

    @GetMapping("/projects/deleted")
    public ResponseEntity<List<ProjectResponse>> getDeletedProjects() {
        List<ProjectResponse> projects = projectService.getDeletedProjects();
        return ResponseEntity.ok(projects);
    }

    @PostMapping("/projects/{projectId}/restore")
    public ResponseEntity<Void> restoreProject(@PathVariable Long projectId) {
        projectService.restoreProject(projectId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tickets/deleted")
    public ResponseEntity<List<TicketResponse>> getDeletedTickets(@RequestParam Long projectId) {
        List<TicketResponse> tickets = ticketService.getDeletedTickets(projectId);
        return ResponseEntity.ok(tickets);
    }

    @PostMapping("/tickets/{ticketId}/restore")
    public ResponseEntity<Void> restoreTicket(@PathVariable Long ticketId) {
        ticketService.restoreTicket(ticketId);
        return ResponseEntity.ok().build();
    }
}
