package com.example.freelance.controller.moderator;

import com.example.freelance.common.dto.ApiResponse;
import com.example.freelance.common.util.ResponseUtil;
import com.example.freelance.common.exception.NotFoundException;
import com.example.freelance.domain.task.Task;
import com.example.freelance.domain.user.User;
import com.example.freelance.domain.user.UserStatus;
import com.example.freelance.repository.task.TaskRepository;
import com.example.freelance.repository.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/moderator")
@RequiredArgsConstructor
@Tag(name = "Moderator", description = "Moderator endpoints for blocking tasks and users")
@SecurityRequirement(name = "Bearer Authentication")
public class ModeratorController {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Operation(
            summary = "Block a task",
            description = "Blocks a task. Only moderators can perform this action."
    )
    @PostMapping("/tasks/{id}/block")
    @PreAuthorize("hasRole('MODERATOR')")
    @Transactional
    public ResponseEntity<ApiResponse<String>> blockTask(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task", id.toString()));

        task.setIsBlocked(true);
        taskRepository.save(task);

        return ResponseEntity.ok(ResponseUtil.success("Task blocked successfully"));
    }

    @Operation(
            summary = "Unblock a task",
            description = "Unblocks a task. Only moderators can perform this action."
    )
    @PostMapping("/tasks/{id}/unblock")
    @PreAuthorize("hasRole('MODERATOR')")
    @Transactional
    public ResponseEntity<ApiResponse<String>> unblockTask(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task", id.toString()));

        task.setIsBlocked(false);
        taskRepository.save(task);

        return ResponseEntity.ok(ResponseUtil.success("Task unblocked successfully"));
    }

    @Operation(
            summary = "Block a user",
            description = "Blocks a user by setting their status to BANNED. Only moderators can perform this action."
    )
    @PostMapping("/users/{id}/block")
    @PreAuthorize("hasRole('MODERATOR')")
    @Transactional
    public ResponseEntity<ApiResponse<String>> blockUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id.toString()));

        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);

        return ResponseEntity.ok(ResponseUtil.success("User blocked successfully"));
    }

    @Operation(
            summary = "Unblock a user",
            description = "Unblocks a user by setting their status to ACTIVE. Only moderators can perform this action."
    )
    @PostMapping("/users/{id}/unblock")
    @PreAuthorize("hasRole('MODERATOR')")
    @Transactional
    public ResponseEntity<ApiResponse<String>> unblockUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id.toString()));

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        return ResponseEntity.ok(ResponseUtil.success("User unblocked successfully"));
    }
}

