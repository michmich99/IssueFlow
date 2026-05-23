package com.issueflow.service;

import com.issueflow.dto.request.CreateCommentRequest;
import com.issueflow.dto.request.UpdateCommentRequest;
import com.issueflow.dto.response.CommentResponse;
import com.issueflow.entity.Comment;
import com.issueflow.entity.Ticket;
import com.issueflow.entity.User;
import com.issueflow.exception.ResourceNotFoundException;
import com.issueflow.repository.CommentRepository;
import com.issueflow.repository.TicketRepository;
import com.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepo;
    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;
    private final MentionService mentionService;
    private final AuditLogService auditLogService;

    @Transactional
    public CommentResponse createComment(Long ticketId, CreateCommentRequest req) {
        Ticket ticket = ticketRepo.findActiveById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User author = userRepo.findById(req.getAuthorId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", req.getAuthorId()));

        Comment comment = new Comment();
        comment.setContent(req.getContent());
        comment.setTicket(ticket);
        comment.setAuthor(author);

        Comment saved = commentRepo.save(comment);
        var mentions = mentionService.extractAndSaveMentions(saved, req.getContent());
        auditLogService.log(com.issueflow.entity.enums.AuditAction.CREATE, 
            com.issueflow.entity.enums.EntityType.COMMENT, saved.getId(), author.getId(), 
            com.issueflow.entity.enums.Actor.USER);
        
        return CommentResponse.from(saved, mentions);
    }

    public List<CommentResponse> getCommentsByTicket(Long ticketId) {
        if (!ticketRepo.findActiveById(ticketId).isPresent()) {
            throw new ResourceNotFoundException("Ticket", "id", ticketId);
        }
        return commentRepo.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
            .map(c -> {
                var mentions = mentionService.getMentionsByComment(c);
                return CommentResponse.from(c, mentions);
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse updateComment(Long id, UpdateCommentRequest req) {
        Comment comment = commentRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        comment.setContent(req.getContent());
        Comment saved = commentRepo.save(comment);
        auditLogService.log(com.issueflow.entity.enums.AuditAction.UPDATE, 
            com.issueflow.entity.enums.EntityType.COMMENT, saved.getId(), null, 
            com.issueflow.entity.enums.Actor.USER);
        
        var mentions = mentionService.getMentionsByComment(saved);
        return CommentResponse.from(saved, mentions);
    }

    @Transactional
    public void deleteComment(Long id) {
        if (!commentRepo.existsById(id)) {
            throw new ResourceNotFoundException("Comment", "id", id);
        }
        commentRepo.deleteById(id);
        auditLogService.log(com.issueflow.entity.enums.AuditAction.DELETE, 
            com.issueflow.entity.enums.EntityType.COMMENT, id, null, 
            com.issueflow.entity.enums.Actor.USER);
    }
}
