package com.issueflow.dto.request;

import com.issueflow.entity.enums.Priority;
import com.issueflow.entity.enums.TicketStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateTicketRequest {
    
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private TicketStatus status;

    private Priority priority;

    private Long assigneeId;

    private LocalDateTime dueDate;
}
