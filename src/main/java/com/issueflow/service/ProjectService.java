package com.issueflow.service;

import com.issueflow.dto.request.CreateProjectRequest;
import com.issueflow.dto.request.UpdateProjectRequest;
import com.issueflow.dto.response.ProjectResponse;
import com.issueflow.entity.Project;
import com.issueflow.entity.User;
import com.issueflow.entity.enums.Actor;
import com.issueflow.entity.enums.AuditAction;
import com.issueflow.entity.enums.EntityType;
import com.issueflow.exception.ResourceNotFoundException;
import com.issueflow.repository.ProjectRepository;
import com.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final AuditLogService auditLogService;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest req) {
        User owner = userRepo.findById(req.getOwnerId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", req.getOwnerId()));

        Project project = new Project();
        project.setName(req.getName());
        project.setDescription(req.getDescription());
        project.setOwner(owner);

        Project saved = projectRepo.save(project);
        auditLogService.log(AuditAction.CREATE, EntityType.PROJECT, saved.getId(), owner.getId(), Actor.USER);
        
        return ProjectResponse.from(saved);
    }

    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepo.findActiveById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return ProjectResponse.from(project);
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepo.findAllActive().stream()
            .map(ProjectResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponse updateProject(Long id, UpdateProjectRequest req) {
        Project project = projectRepo.findActiveById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        if (req.getName() != null) {
            project.setName(req.getName());
        }
        if (req.getDescription() != null) {
            project.setDescription(req.getDescription());
        }

        Project saved = projectRepo.save(project);
        auditLogService.log(AuditAction.UPDATE, EntityType.PROJECT, saved.getId(), null, Actor.USER);
        
        return ProjectResponse.from(saved);
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepo.findActiveById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        
        project.setDeletedAt(LocalDateTime.now());
        projectRepo.save(project);
        auditLogService.log(AuditAction.DELETE, EntityType.PROJECT, id, null, Actor.USER);
    }

    public List<ProjectResponse> getDeletedProjects() {
        return projectRepo.findAllDeleted().stream()
            .map(ProjectResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public void restoreProject(Long id) {
        Project project = projectRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        
        if (project.getDeletedAt() == null) {
            throw new ResourceNotFoundException("Deleted project", "id", id);
        }
        
        project.setDeletedAt(null);
        projectRepo.save(project);
        auditLogService.log(AuditAction.RESTORE, EntityType.PROJECT, id, null, Actor.USER);
    }
}
