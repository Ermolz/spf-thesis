package com.example.freelance.dto.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAttachmentResponse {
    private Long id;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private Long uploadedById;
    private String uploadedByEmail;
    private Instant createdAt;
}

