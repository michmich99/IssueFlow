package com.issueflow.controller;

import com.issueflow.dto.response.MentionsPaginatedResponse;
import com.issueflow.service.MentionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/mentions")
@RequiredArgsConstructor
public class MentionController {

    private final MentionService mentionService;

    @GetMapping
    public ResponseEntity<MentionsPaginatedResponse> getMentions(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        
        MentionsPaginatedResponse response = mentionService.getMentionsByUser(userId, page, pageSize);
        return ResponseEntity.ok(response);
    }
}
