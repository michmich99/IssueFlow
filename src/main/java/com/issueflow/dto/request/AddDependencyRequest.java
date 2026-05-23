package com.issueflow.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddDependencyRequest {
    @NotNull(message = "Blocked by ticket ID is required")
    private Long blockedBy;
}
