package com.example.freelance.dto.task;

import com.example.freelance.domain.task.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private Long assignmentId;
    private String title;
    private String description;
    private TaskStatus status;
    private Instant deadline;
    private List<TaskAttachmentResponse> attachments;
    private Instant createdAt;
    private Instant updatedAt;
}

