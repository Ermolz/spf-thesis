package com.example.freelance.service.task;

import com.example.freelance.common.exception.BadRequestException;
import com.example.freelance.common.exception.ForbiddenException;
import com.example.freelance.common.exception.NotFoundException;
import com.example.freelance.domain.assignment.Assignment;
import com.example.freelance.domain.assignment.AssignmentStatus;
import com.example.freelance.domain.task.Task;
import com.example.freelance.domain.task.TaskAttachment;
import com.example.freelance.domain.task.TaskStatus;
import com.example.freelance.domain.user.User;
import com.example.freelance.dto.task.CreateTaskRequest;
import com.example.freelance.dto.task.TaskAttachmentResponse;
import com.example.freelance.dto.task.TaskResponse;
import com.example.freelance.dto.task.UpdateTaskRequest;
import com.example.freelance.mapper.task.TaskAttachmentMapper;
import com.example.freelance.mapper.task.TaskMapper;
import com.example.freelance.repository.assignment.AssignmentRepository;
import com.example.freelance.repository.task.TaskAttachmentRepository;
import com.example.freelance.repository.task.TaskRepository;
import com.example.freelance.repository.user.UserRepository;
import com.example.freelance.security.UserPrincipal;
import com.example.freelance.util.FileStorageUtil;
import com.example.freelance.util.FileValidationUtil;
import com.example.freelance.common.util.MdcUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final FileStorageUtil fileStorageUtil;
    private final FileValidationUtil fileValidationUtil;
    private final TaskMapper taskMapper;
    private final TaskAttachmentMapper taskAttachmentMapper;

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new NotFoundException("Assignment", request.getAssignmentId().toString()));

        validateAssignmentAccess(assignment, userPrincipal.getId());

        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new BadRequestException("Can only create tasks for active assignments", "ASSIGNMENT_NOT_ACTIVE");
        }

        Task task = new Task();
        task.setAssignment(assignment);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO);
        task.setDeadline(request.getDeadline());

        task = taskRepository.save(task);
        
        MdcUtil.setUserId(userPrincipal.getId());
        MdcUtil.setOperation("CREATE_TASK");
        log.info("Task created: taskId={}, assignmentId={}, title={}, status={}, createdBy={}", 
                task.getId(), assignment.getId(), task.getTitle(), task.getStatus(), userPrincipal.getId());
        MdcUtil.clearCustomValues();
        
        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task", taskId.toString()));

        validateAssignmentAccess(task.getAssignment(), userPrincipal.getId());

        TaskStatus oldStatus = task.getStatus();
        boolean statusChanged = false;
        
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null && !request.getStatus().equals(oldStatus)) {
            task.setStatus(request.getStatus());
            statusChanged = true;
        }
        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }
        
        if (statusChanged) {
            MdcUtil.setUserId(userPrincipal.getId());
            MdcUtil.setOperation("UPDATE_TASK_STATUS");
            log.info("Task status changed: taskId={}, assignmentId={}, title={}, oldStatus={}, newStatus={}, changedBy={}", 
                    taskId, task.getAssignment().getId(), task.getTitle(), oldStatus, request.getStatus(), userPrincipal.getId());
            MdcUtil.clearCustomValues();
        }

        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        UserPrincipal userPrincipal = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task", taskId.toString()));

        validateAssignmentAccess(task.getAssignment(), userPrincipal.getId());

        return mapToResponse(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByAssignment(Long assignmentId, Pageable pageable) {
        UserPrincipal userPrincipal = getCurrentUser();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment", assignmentId.toString()));

        validateAssignmentAccess(assignment, userPrincipal.getId());

        Page<Task> tasks = taskRepository.findByAssignmentId(assignmentId, pageable);
        return tasks.map(this::mapToResponse);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        UserPrincipal userPrincipal = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task", taskId.toString()));

        validateAssignmentAccess(task.getAssignment(), userPrincipal.getId());

        List<TaskAttachment> attachments = taskAttachmentRepository.findByTaskId(taskId);
        attachments.forEach(attachment -> fileStorageUtil.deleteFile(attachment.getFilePath()));

        taskRepository.delete(task);
    }

    @Transactional
    public TaskAttachmentResponse uploadAttachment(Long taskId, MultipartFile file) {
        UserPrincipal userPrincipal = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task", taskId.toString()));

        validateAssignmentAccess(task.getAssignment(), userPrincipal.getId());

        if (file.isEmpty()) {
            throw new BadRequestException("File cannot be empty", "EMPTY_FILE");
        }

        try {
            fileValidationUtil.validateFile(file);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage(), "INVALID_FILE_TYPE");
        }

        try {
            String filePath = fileStorageUtil.storeFile(file, "tasks/" + taskId);
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new NotFoundException("User", userPrincipal.getId().toString()));

            TaskAttachment attachment = new TaskAttachment();
            attachment.setTask(task);
            attachment.setFilePath(filePath);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());
            attachment.setUploadedBy(user);

            attachment = taskAttachmentRepository.save(attachment);
            return taskAttachmentMapper.toResponse(attachment);
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new BadRequestException("Failed to upload file", "FILE_UPLOAD_FAILED");
        }
    }

    @Transactional
    public void deleteAttachment(Long taskId, Long attachmentId) {
        UserPrincipal userPrincipal = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task", taskId.toString()));

        validateAssignmentAccess(task.getAssignment(), userPrincipal.getId());

        TaskAttachment attachment = taskAttachmentRepository.findByIdAndTaskId(attachmentId, taskId)
                .orElseThrow(() -> new NotFoundException("TaskAttachment", attachmentId.toString()));

        fileStorageUtil.deleteFile(attachment.getFilePath());
        taskAttachmentRepository.delete(attachment);
    }

    @Transactional(readOnly = true)
    public List<TaskAttachmentResponse> getTaskAttachments(Long taskId) {
        UserPrincipal userPrincipal = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task", taskId.toString()));

        validateAssignmentAccess(task.getAssignment(), userPrincipal.getId());

        List<TaskAttachment> attachments = taskAttachmentRepository.findByTaskId(taskId);
        return attachments.stream()
                .map(taskAttachmentMapper::toResponse)
                .toList();
    }

    private void validateAssignmentAccess(Assignment assignment, Long userId) {
        boolean isClient = assignment.getProject().getClient().getUser().getId().equals(userId);
        boolean isFreelancer = assignment.getFreelancer().getUser().getId().equals(userId);

        if (!isClient && !isFreelancer) {
            throw new ForbiddenException("Access denied to this assignment", "ACCESS_DENIED");
        }
    }

    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private TaskResponse mapToResponse(Task task) {
        TaskResponse response = taskMapper.toResponse(task);
        List<TaskAttachmentResponse> attachments = taskAttachmentRepository.findByTaskId(task.getId())
                .stream()
                .map(taskAttachmentMapper::toResponse)
                .toList();
        response.setAttachments(attachments);
        return response;
    }
}

