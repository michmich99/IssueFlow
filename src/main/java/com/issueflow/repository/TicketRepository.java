package com.issueflow.repository;

import com.issueflow.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("SELECT t FROM Ticket t WHERE t.project.id = :projectId AND t.deletedAt IS NULL")
    List<Ticket> findByProjectId(Long projectId);
    
    @Query("SELECT t FROM Ticket t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Ticket> findActiveById(Long id);
    
    @Query("SELECT t FROM Ticket t WHERE t.project.id = :projectId AND t.deletedAt IS NOT NULL")
    List<Ticket> findDeletedByProjectId(Long projectId);
}
