package com.issueflow.service;

import com.issueflow.dto.response.DependencyTicketResponse;
import com.issueflow.entity.Ticket;
import com.issueflow.entity.TicketDependency;
import com.issueflow.exception.BadRequestException;
import com.issueflow.exception.ResourceNotFoundException;
import com.issueflow.repository.TicketDependencyRepository;
import com.issueflow.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DependencyService {

    private final TicketDependencyRepository dependencyRepo;
    private final TicketRepository ticketRepo;

    @Transactional
    public void addDependency(Long ticketId, Long blockedById) {
        Ticket ticket = ticketRepo.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        
        Ticket blockedBy = ticketRepo.findById(blockedById)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", blockedById));

        if (ticketId.equals(blockedById)) {
            throw new BadRequestException("A ticket cannot block itself");
        }

        if (dependencyRepo.findByTicketIdAndBlockedById(ticketId, blockedById).isPresent()) {
            throw new BadRequestException("Dependency already exists");
        }

        TicketDependency dependency = new TicketDependency();
        dependency.setTicket(ticket);
        dependency.setBlockedBy(blockedBy);
        dependencyRepo.save(dependency);
    }

    public List<DependencyTicketResponse> getDependencies(Long ticketId) {
        if (!ticketRepo.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket", "id", ticketId);
        }
        
        return dependencyRepo.findByTicketId(ticketId).stream()
            .map(dep -> DependencyTicketResponse.from(dep.getBlockedBy()))
            .collect(Collectors.toList());
    }

    @Transactional
    public void removeDependency(Long ticketId, Long blockerId) {
        if (!ticketRepo.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket", "id", ticketId);
        }
        
        if (dependencyRepo.findByTicketIdAndBlockedById(ticketId, blockerId).isEmpty()) {
            throw new ResourceNotFoundException("Dependency", "blockerId", blockerId);
        }

        dependencyRepo.deleteByTicketIdAndBlockedById(ticketId, blockerId);
    }
}
