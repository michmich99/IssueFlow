package com.issueflow.service;

import com.issueflow.dto.request.CreateTicketRequest;
import com.issueflow.dto.request.UpdateTicketRequest;
import com.issueflow.dto.response.TicketResponse;
import com.issueflow.entity.Project;
import com.issueflow.entity.Ticket;
import com.issueflow.entity.User;
import com.issueflow.entity.enums.Actor;
import com.issueflow.entity.enums.AuditAction;
import com.issueflow.entity.enums.EntityType;
import com.issueflow.entity.enums.TicketStatus;
import com.issueflow.exception.BadRequestException;
import com.issueflow.exception.ResourceNotFoundException;
import com.issueflow.repository.ProjectRepository;
import com.issueflow.repository.TicketRepository;
import com.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepo;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final AuditLogService auditLogService;
    private final WorkloadService workloadService;

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest req) {
        Project project = projectRepo.findActiveById(req.getProjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Project", "id", req.getProjectId()));

        Ticket ticket = new Ticket();
        ticket.setTitle(req.getTitle());
        ticket.setDescription(req.getDescription());
        ticket.setStatus(req.getStatus());
        ticket.setPriority(req.getPriority());
        ticket.setType(req.getType());
        ticket.setProject(project);
        ticket.setDueDate(req.getDueDate());
        ticket.setIsOverdue(false);

        if (req.getAssigneeId() != null) {
            User assignee = userRepo.findById(req.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", req.getAssigneeId()));
            ticket.setAssignee(assignee);
        } else {
            User autoAssignee = workloadService.findLeastLoadedDeveloper(req.getProjectId());
            if (autoAssignee != null) {
                ticket.setAssignee(autoAssignee);
            }
        }

        Ticket saved = ticketRepo.save(ticket);
        Actor actor = (req.getAssigneeId() == null && saved.getAssignee() != null) ? Actor.SYSTEM : Actor.USER;
        auditLogService.log(AuditAction.CREATE, EntityType.TICKET, saved.getId(), 
            saved.getAssignee() != null ? saved.getAssignee().getId() : null, actor);
        
        return TicketResponse.from(saved);
    }

    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepo.findActiveById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        return TicketResponse.from(ticket);
    }

    public List<TicketResponse> getTicketsByProject(Long projectId) {
        if (!projectRepo.findActiveById(projectId).isPresent()) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return ticketRepo.findByProjectId(projectId).stream()
            .map(TicketResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public TicketResponse updateTicket(Long id, UpdateTicketRequest req) {
        Ticket ticket = ticketRepo.findActiveById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        if (ticket.getStatus() == TicketStatus.DONE) {
            throw new BadRequestException("Cannot update ticket with DONE status");
        }

        if (req.getTitle() != null) {
            ticket.setTitle(req.getTitle());
        }
        if (req.getDescription() != null) {
            ticket.setDescription(req.getDescription());
        }
        if (req.getPriority() != null) {
            ticket.setPriority(req.getPriority());
        }
        if (req.getStatus() != null) {
            if (!ticket.getStatus().canTransitionTo(req.getStatus())) {
                throw new BadRequestException(
                    String.format("Invalid status transition from %s to %s", 
                        ticket.getStatus(), req.getStatus()));
            }
            ticket.setStatus(req.getStatus());
        }
        if (req.getAssigneeId() != null) {
            User assignee = userRepo.findById(req.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", req.getAssigneeId()));
            ticket.setAssignee(assignee);
        }
        if (req.getDueDate() != null) {
            ticket.setDueDate(req.getDueDate());
        }

        Ticket saved = ticketRepo.save(ticket);
        auditLogService.log(AuditAction.UPDATE, EntityType.TICKET, saved.getId(), null, Actor.USER);
        
        return TicketResponse.from(saved);
    }

    @Transactional
    public void deleteTicket(Long id) {
        Ticket ticket = ticketRepo.findActiveById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        
        ticket.setDeletedAt(LocalDateTime.now());
        ticketRepo.save(ticket);
        auditLogService.log(AuditAction.DELETE, EntityType.TICKET, id, null, Actor.USER);
    }

    public List<TicketResponse> getDeletedTickets(Long projectId) {
        return ticketRepo.findDeletedByProjectId(projectId).stream()
            .map(TicketResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public void restoreTicket(Long id) {
        Ticket ticket = ticketRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        
        if (ticket.getDeletedAt() == null) {
            throw new ResourceNotFoundException("Deleted ticket", "id", id);
        }
        
        ticket.setDeletedAt(null);
        ticketRepo.save(ticket);
        auditLogService.log(AuditAction.RESTORE, EntityType.TICKET, id, null, Actor.USER);
    }
}
