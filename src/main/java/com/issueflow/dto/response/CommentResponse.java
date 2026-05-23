package com.issueflow.dto.response;

import com.issueflow.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private Long ticketId;
    private Long authorId;
    private String authorUsername;
    private String content;
    private List<MentionedUserResponse> mentionedUsers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getTicket().getId(),
            comment.getAuthor().getId(),
            comment.getAuthor().getUsername(),
            comment.getContent(),
            null,
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );
    }
    
    public static CommentResponse from(Comment comment, List<MentionedUserResponse> mentions) {
        return new CommentResponse(
            comment.getId(),
            comment.getTicket().getId(),
            comment.getAuthor().getId(),
            comment.getAuthor().getUsername(),
            comment.getContent(),
            mentions,
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );
    }
}
