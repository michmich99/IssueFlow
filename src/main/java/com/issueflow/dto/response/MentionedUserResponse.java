package com.issueflow.dto.response;

import com.issueflow.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MentionedUserResponse {
    private Long id;
    private String username;
    private String fullName;

    public static MentionedUserResponse from(User user) {
        return new MentionedUserResponse(
            user.getId(),
            user.getUsername(),
            user.getFullName()
        );
    }
}
