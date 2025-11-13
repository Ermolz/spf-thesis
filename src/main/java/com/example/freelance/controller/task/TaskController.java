package com.example.freelance.controller.task;

import com.example.freelance.common.dto.ApiResponse;
import com.example.freelance.common.util.ResponseUtil;
import com.example.freelance.dto.task.CreateTaskRequest;
import com.example.freelance.dto.task.TaskAttachmentResponse;
import com.example.freelance.dto.task.TaskResponse;
import com.example.freelance.dto.task.UpdateTaskRequest;
import com.example.freelance.service.task.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tasks", description = "Task management endpoints. Tasks break down assignments into trackable work items. Both clients and freelancers can create and manage tasks for their assignments.")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
public class TaskController {
    private final TaskService taskService;

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create a new task",
            description = """
                    Creates a new task for an assignment. Both client and freelancer can create tasks for their assignments.
                    
                    **Task Creation:**
                    - Task is created with TODO status by default
                    - Both parties can track progress through task status
                    - Tasks can have attachments for file sharing
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Task creation details",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CreateTaskRequest.class)
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Task successfully created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this assignment"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Update a task",
            description = """
                    Updates an existing task. Both client and freelancer can update tasks for their assignments.
                    
                    **Update Rules:**
                    - Can update title, description, status, and deadline
                    - Status transitions must be valid (TODO → IN_PROGRESS → COMPLETED)
                    """,
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "Task unique identifier", required = true, example = "1")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task successfully updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this assignment"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get task by ID",
            description = "Retrieves a single task by its ID. Only the client or freelancer associated with the assignment can view it.",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "Task unique identifier", required = true, example = "1")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this assignment"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long id) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get tasks by assignment",
            description = """
                    Retrieves paginated list of tasks for a specific assignment.
                    
                    **Pagination:**
                    - Default page size: 20
                    - Default sort: createdAt DESC (newest first)
                    """,
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "assignmentId", description = "Assignment unique identifier", required = true, example = "1"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "Page size", example = "20"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "Sort field and direction", example = "createdAt,desc")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this assignment"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasksByAssignment(
            @PathVariable Long assignmentId,
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<TaskResponse> response = taskService.getTasksByAssignment(assignmentId, pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Delete a task",
            description = "Deletes a task. Only the client or freelancer associated with the assignment can delete tasks.",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "Task unique identifier", required = true, example = "1")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Task successfully deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this assignment"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Upload task attachment",
            description = """
                    Uploads a file attachment to a task. Maximum file size is 10MB.
                    
                    **Supported File Types (Whitelist):**
                    - Documents: PDF, DOC, DOCX
                    - Images: JPG, JPEG, PNG, GIF
                    - Archives: ZIP, RAR
                    
                    **Security:**
                    - File extension and MIME type are validated
                    - Only whitelisted file types are accepted
                    - File type spoofing is prevented by cross-validation
                    """,
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "Task unique identifier", required = true, example = "1"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "file", description = "File to upload", required = true)
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Attachment successfully uploaded"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - File too large or invalid format"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this assignment"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PostMapping("/{id}/attachments")
    public ResponseEntity<ApiResponse<TaskAttachmentResponse>> uploadAttachment(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        TaskAttachmentResponse response = taskService.uploadAttachment(id, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get task attachments",
            description = "Retrieves list of all attachments for a task.",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "Task unique identifier", required = true, example = "1")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Attachments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this assignment"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{id}/attachments")
    public ResponseEntity<ApiResponse<List<TaskAttachmentResponse>>> getTaskAttachments(@PathVariable Long id) {
        List<TaskAttachmentResponse> response = taskService.getTaskAttachments(id);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Delete task attachment",
            description = "Deletes a task attachment. Only the user who uploaded it or the assignment owner can delete.",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "taskId", description = "Task unique identifier", required = true, example = "1"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "attachmentId", description = "Attachment unique identifier", required = true, example = "1")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Attachment successfully deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to delete this attachment"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task or attachment not found")
    })
    @DeleteMapping("/{taskId}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long taskId,
            @PathVariable Long attachmentId) {
        taskService.deleteAttachment(taskId, attachmentId);
        return ResponseEntity.noContent().build();
    }
}

