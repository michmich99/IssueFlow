package com.issueflow.controller;

import com.issueflow.dto.request.AddDependencyRequest;
import com.issueflow.dto.response.DependencyTicketResponse;
import com.issueflow.service.DependencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets/{ticketId}/dependencies")
@RequiredArgsConstructor
public class DependencyController {

    private final DependencyService dependencyService;

    @PostMapping
    public ResponseEntity<Void> addDependency(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddDependencyRequest req) {
        dependencyService.addDependency(ticketId, req.getBlockedBy());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<DependencyTicketResponse>> getDependencies(@PathVariable Long ticketId) {
        List<DependencyTicketResponse> dependencies = dependencyService.getDependencies(ticketId);
        return ResponseEntity.ok(dependencies);
    }

    @DeleteMapping("/{blockerId}")
    public ResponseEntity<Void> removeDependency(
            @PathVariable Long ticketId,
            @PathVariable Long blockerId) {
        dependencyService.removeDependency(ticketId, blockerId);
        return ResponseEntity.ok().build();
    }
}
