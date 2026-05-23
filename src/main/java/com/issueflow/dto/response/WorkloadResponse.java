package com.issueflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadResponse {
    private Long userId;
    private String username;
    private Long openTicketCount;
}
