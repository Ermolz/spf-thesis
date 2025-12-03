package com.example.freelance.service.task;

import com.example.freelance.common.exception.ForbiddenException;
import com.example.freelance.domain.assignment.Assignment;
import com.example.freelance.domain.assignment.AssignmentStatus;
import com.example.freelance.domain.project.Project;
import com.example.freelance.domain.task.Task;
import com.example.freelance.domain.task.TaskStatus;
import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.domain.user.FreelancerProfile;
import com.example.freelance.domain.user.User;
import com.example.freelance.dto.task.CreateTaskRequest;
import com.example.freelance.dto.task.TaskResponse;
import com.example.freelance.mapper.task.TaskAttachmentMapper;
import com.example.freelance.mapper.task.TaskMapper;
import com.example.freelance.repository.assignment.AssignmentRepository;
import com.example.freelance.repository.task.TaskAttachmentRepository;
import com.example.freelance.repository.task.TaskRepository;
import com.example.freelance.repository.user.UserRepository;
import com.example.freelance.security.UserPrincipal;
import com.example.freelance.util.FileStorageUtil;
import com.example.freelance.util.FileValidationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private TaskAttachmentRepository taskAttachmentRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileStorageUtil fileStorageUtil;
    @Mock private FileValidationUtil fileValidationUtil;
    @Mock private TaskMapper taskMapper;
    @Mock private TaskAttachmentMapper taskAttachmentMapper;

    private TaskService taskService;

    private static final Long AUTH_USER_ID = 100L;
    private static final Long ASSIGNMENT_ID = 1L;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(
                taskRepository,
                taskAttachmentRepository,
                assignmentRepository,
                userRepository,
                fileStorageUtil,
                fileValidationUtil,
                taskMapper,
                taskAttachmentMapper
        );

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserPrincipal principal = mock(UserPrincipal.class);

        when(principal.getId()).thenReturn(AUTH_USER_ID);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTask_WhenUserIsParticipant_ShouldCreateTask() {
        CreateTaskRequest request = new CreateTaskRequest(ASSIGNMENT_ID, "Fix bug", "Desc", TaskStatus.TODO, Instant.now());

        User clientUser = new User(); clientUser.setId(AUTH_USER_ID);
        ClientProfile clientProfile = new ClientProfile(); clientProfile.setUser(clientUser);
        Project project = new Project(); project.setClient(clientProfile);

        User freelancerUser = new User(); freelancerUser.setId(200L);
        FreelancerProfile freelancerProfile = new FreelancerProfile(); freelancerProfile.setUser(freelancerUser);

        Assignment assignment = new Assignment();
        assignment.setId(ASSIGNMENT_ID);
        assignment.setProject(project);
        assignment.setFreelancer(freelancerProfile);
        assignment.setStatus(AssignmentStatus.ACTIVE);

        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));

        Task savedTask = new Task();
        savedTask.setId(50L);
        savedTask.setTitle(request.getTitle());

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        when(taskAttachmentRepository.findByTaskId(any())).thenReturn(Collections.emptyList());

        TaskResponse expectedResponse = TaskResponse.builder().id(50L).title(request.getTitle()).build();
        when(taskMapper.toResponse(savedTask)).thenReturn(expectedResponse);

        TaskResponse response = taskService.createTask(request);

        assertThat(response.getId()).isEqualTo(50L);
        assertThat(response.getTitle()).isEqualTo("Fix bug");
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_WhenUserIsNotParticipant_ShouldThrowForbiddenException() {
        CreateTaskRequest request = new CreateTaskRequest(ASSIGNMENT_ID, "Hacking", "Desc", TaskStatus.TODO, null);

        User otherClientUser = new User(); otherClientUser.setId(999L);
        ClientProfile clientProfile = new ClientProfile(); clientProfile.setUser(otherClientUser);
        Project project = new Project(); project.setClient(clientProfile);

        User freelancerUser = new User(); freelancerUser.setId(200L);
        FreelancerProfile freelancerProfile = new FreelancerProfile(); freelancerProfile.setUser(freelancerUser);

        Assignment assignment = new Assignment();
        assignment.setProject(project);
        assignment.setFreelancer(freelancerProfile);

        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));

        assertThrows(ForbiddenException.class, () -> taskService.createTask(request));
        verify(taskRepository, never()).save(any());
    }
}