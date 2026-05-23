package com.issueflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MentionsPaginatedResponse {
    private List<CommentResponse> data;
    private Long total;
    private Integer page;
}
