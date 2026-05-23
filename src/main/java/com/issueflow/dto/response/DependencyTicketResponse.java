package com.issueflow.dto.response;

import com.issueflow.entity.Ticket;
import com.issueflow.entity.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DependencyTicketResponse {
    private Long id;
    private String title;
    private TicketStatus status;

    public static DependencyTicketResponse from(Ticket ticket) {
        return new DependencyTicketResponse(
            ticket.getId(),
            ticket.getTitle(),
            ticket.getStatus()
        );
    }
}
