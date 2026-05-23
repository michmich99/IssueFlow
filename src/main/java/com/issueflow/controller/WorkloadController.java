package com.issueflow.controller;

import com.issueflow.dto.response.WorkloadResponse;
import com.issueflow.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/workload")
@RequiredArgsConstructor
public class WorkloadController {

    private final WorkloadService workloadService;

    @GetMapping
    public ResponseEntity<List<WorkloadResponse>> getProjectWorkload(@PathVariable Long projectId) {
        List<WorkloadResponse> workload = workloadService.getProjectWorkload(projectId);
        return ResponseEntity.ok(workload);
    }
}
