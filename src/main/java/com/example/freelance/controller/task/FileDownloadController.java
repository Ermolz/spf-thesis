package com.example.freelance.controller.task;

import com.example.freelance.common.exception.ForbiddenException;
import com.example.freelance.common.exception.NotFoundException;
import com.example.freelance.domain.assignment.Assignment;
import com.example.freelance.domain.task.TaskAttachment;
import com.example.freelance.domain.user.FreelancerProfile;
import com.example.freelance.repository.task.TaskAttachmentRepository;
import com.example.freelance.repository.user.FreelancerProfileRepository;
import com.example.freelance.security.UserPrincipal;
import com.example.freelance.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "File Downloads", description = "File download endpoints for task attachments and portfolios.")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
public class FileDownloadController {
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final FileStorageUtil fileStorageUtil;

    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("TaskAttachment", attachmentId.toString()));

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Assignment assignment = attachment.getTask().getAssignment();
        boolean isClient = assignment.getProject().getClient().getUser().getId().equals(userPrincipal.getId());
        boolean isFreelancer = assignment.getFreelancer().getUser().getId().equals(userPrincipal.getId());

        if (!isClient && !isFreelancer) {
            throw new ForbiddenException("Access denied to this file", "ACCESS_DENIED");
        }

        try {
            Path filePath = fileStorageUtil.getFilePath(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new NotFoundException("File", attachment.getFilePath());
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getContentType() != null
                            ? attachment.getContentType()
                            : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + attachment.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new NotFoundException("File", attachment.getFilePath());
        }
    }

    @GetMapping("/portfolios/{freelancerId}")
    public ResponseEntity<Resource> downloadPortfolio(@PathVariable Long freelancerId) {
        // Portfolio is accessible to all users (permitAll in SecurityConfig)
        // If you want to restrict access, change SecurityConfig to require authentication
        
        FreelancerProfile profile = freelancerProfileRepository.findById(freelancerId)
                .orElseThrow(() -> new NotFoundException("FreelancerProfile", freelancerId.toString()));

        if (profile.getPortfolioFilePath() == null) {
            throw new NotFoundException("Portfolio", freelancerId.toString());
        }

        try {
            Path filePath = fileStorageUtil.getFilePath(profile.getPortfolioFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new NotFoundException("Portfolio file", profile.getPortfolioFilePath());
            }

            String fileName = profile.getPortfolioFilePath().substring(profile.getPortfolioFilePath().lastIndexOf("/") + 1);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new NotFoundException("Portfolio file", profile.getPortfolioFilePath());
        }
    }
}

