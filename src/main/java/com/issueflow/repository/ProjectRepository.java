package com.issueflow.repository;

import com.issueflow.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("SELECT p FROM Project p WHERE p.deletedAt IS NULL")
    List<Project> findAllActive();
    
    @Query("SELECT p FROM Project p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Project> findActiveById(Long id);
    
    @Query("SELECT p FROM Project p WHERE p.deletedAt IS NOT NULL")
    List<Project> findAllDeleted();
}
