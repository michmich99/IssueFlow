package com.issueflow.dto.request;

import com.issueflow.entity.enums.Role;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    private Role role;
}
