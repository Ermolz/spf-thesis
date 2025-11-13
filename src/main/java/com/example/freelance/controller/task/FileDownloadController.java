package com.example.freelance.controller.task;

import com.example.freelance.common.exception.NotFoundException;
import com.example.freelance.domain.task.TaskAttachment;
import com.example.freelance.repository.task.TaskAttachmentRepository;
import com.example.freelance.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "File Downloads", description = "File download endpoints for task attachments.")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
public class FileDownloadController {
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final FileStorageUtil fileStorageUtil;

    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("TaskAttachment", attachmentId.toString()));

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
}

