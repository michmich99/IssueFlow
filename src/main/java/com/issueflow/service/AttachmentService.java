package com.issueflow.service;

import com.issueflow.dto.response.AttachmentResponse;
import com.issueflow.entity.Attachment;
import com.issueflow.entity.Ticket;
import com.issueflow.entity.User;
import com.issueflow.exception.BadRequestException;
import com.issueflow.exception.ResourceNotFoundException;
import com.issueflow.repository.AttachmentRepository;
import com.issueflow.repository.TicketRepository;
import com.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepo;
    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.max-size:10485760}")
    private long maxFileSize;

    @Transactional
    public AttachmentResponse uploadAttachment(Long ticketId, MultipartFile file, Long uploadedBy) {
        Ticket ticket = ticketRepo.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User uploader = userRepo.findById(uploadedBy)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", uploadedBy));

        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum allowed size");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String storedFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(storedFilename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Attachment attachment = new Attachment();
            attachment.setTicket(ticket);
            attachment.setFilename(originalFilename);
            attachment.setContentType(file.getContentType());
            attachment.setFileSize(file.getSize());
            attachment.setStoragePath(storedFilename);
            attachment.setUploadedBy(uploader);

            Attachment saved = attachmentRepo.save(attachment);
            return AttachmentResponse.from(saved);

        } catch (IOException e) {
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteAttachment(Long attachmentId) {
        Attachment attachment = attachmentRepo.findById(attachmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", attachmentId));

        try {
            Path filePath = Paths.get(uploadDir).resolve(attachment.getStoragePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new BadRequestException("Failed to delete file: " + e.getMessage());
        }

        attachmentRepo.delete(attachment);
    }
}
