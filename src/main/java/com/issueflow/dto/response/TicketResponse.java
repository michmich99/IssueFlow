package com.issueflow.dto.response;

import com.issueflow.entity.Ticket;
import com.issueflow.entity.enums.Priority;
import com.issueflow.entity.enums.TicketStatus;
import com.issueflow.entity.enums.TicketType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private Long id;
    private String title;
    private String description;
    private TicketStatus status;
    private Priority priority;
    private TicketType type;
    private Long projectId;
    private Long assigneeId;
    private String assigneeUsername;
    private LocalDateTime dueDate;
    private Boolean isOverdue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
            ticket.getId(),
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getStatus(),
            ticket.getPriority(),
            ticket.getType(),
            ticket.getProject().getId(),
            ticket.getAssignee() != null ? ticket.getAssignee().getId() : null,
            ticket.getAssignee() != null ? ticket.getAssignee().getUsername() : null,
            ticket.getDueDate(),
            ticket.getIsOverdue(),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt()
        );
    }
}
