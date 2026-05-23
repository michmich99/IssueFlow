package com.issueflow.repository;

import com.issueflow.entity.Mention;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MentionRepository extends JpaRepository<Mention, Long> {
    @Query("SELECT m FROM Mention m WHERE m.mentionedUser.id = :userId ORDER BY m.createdAt DESC")
    Page<Mention> findByMentionedUserId(Long userId, Pageable pageable);
}
