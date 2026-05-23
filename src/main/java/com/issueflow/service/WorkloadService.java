package com.issueflow.service;

import com.issueflow.dto.response.WorkloadResponse;
import com.issueflow.entity.User;
import com.issueflow.entity.enums.Role;
import com.issueflow.entity.enums.TicketStatus;
import com.issueflow.exception.ResourceNotFoundException;
import com.issueflow.repository.ProjectRepository;
import com.issueflow.repository.TicketRepository;
import com.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkloadService {

    private final ProjectRepository projectRepo;
    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;

    public List<WorkloadResponse> getProjectWorkload(Long projectId) {
        if (!projectRepo.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }

        List<User> developers = userRepo.findAll().stream()
            .filter(u -> u.getRole() == Role.DEVELOPER)
            .collect(Collectors.toList());

        return developers.stream()
            .map(dev -> {
                long count = ticketRepo.findByProjectId(projectId).stream()
                    .filter(t -> t.getAssignee() != null && t.getAssignee().getId().equals(dev.getId()))
                    .filter(t -> t.getStatus() != TicketStatus.DONE)
                    .filter(t -> t.getDeletedAt() == null)
                    .count();

                return new WorkloadResponse(dev.getId(), dev.getUsername(), count);
            })
            .collect(Collectors.toList());
    }

    public User findLeastLoadedDeveloper(Long projectId) {
        List<WorkloadResponse> workload = getProjectWorkload(projectId);
        
        return workload.stream()
            .min((w1, w2) -> Long.compare(w1.getOpenTicketCount(), w2.getOpenTicketCount()))
            .map(w -> userRepo.findById(w.getUserId()).orElse(null))
            .orElse(null);
    }
}
