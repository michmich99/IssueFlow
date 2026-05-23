package com.issueflow.scheduler;

import com.issueflow.entity.Ticket;
import com.issueflow.entity.enums.Actor;
import com.issueflow.entity.enums.AuditAction;
import com.issueflow.entity.enums.EntityType;
import com.issueflow.entity.enums.Priority;
import com.issueflow.repository.TicketRepository;
import com.issueflow.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoEscalationScheduler {

    private final TicketRepository ticketRepo;
    private final AuditLogService auditLogService;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void escalateOverdueTickets() {
        log.info("Running auto-escalation job");

        List<Ticket> tickets = ticketRepo.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Ticket ticket : tickets) {
            if (ticket.getDueDate() != null && ticket.getDueDate().isBefore(now)) {
                if (!ticket.getIsOverdue()) {
                    ticket.setIsOverdue(true);
                    
                    if (ticket.getPriority() != Priority.CRITICAL) {
                        Priority oldPriority = ticket.getPriority();
                        Priority newPriority = escalatePriority(oldPriority);
                        ticket.setPriority(newPriority);
                        
                        ticketRepo.save(ticket);
                        auditLogService.log(
                            AuditAction.UPDATE,
                            EntityType.TICKET,
                            ticket.getId(),
                            null,
                            Actor.SYSTEM
                        );
                        
                        log.info("Escalated ticket {} from {} to {}", 
                            ticket.getId(), oldPriority, newPriority);
                    } else {
                        ticketRepo.save(ticket);
                    }
                }
            }
        }

        log.info("Auto-escalation job completed");
    }

    private Priority escalatePriority(Priority current) {
        return switch (current) {
            case LOW -> Priority.MEDIUM;
            case MEDIUM -> Priority.HIGH;
            case HIGH -> Priority.CRITICAL;
            case CRITICAL -> Priority.CRITICAL;
        };
    }
}
