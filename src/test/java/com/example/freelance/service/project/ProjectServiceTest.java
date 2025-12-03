package com.example.freelance.service.project;

import com.example.freelance.common.exception.BadRequestException;
import com.example.freelance.domain.project.Project;
import com.example.freelance.domain.project.ProjectStatus;
import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.dto.project.CreateProjectRequest;
import com.example.freelance.dto.project.ProjectResponse;
import com.example.freelance.mapper.project.ProjectMapper;
import com.example.freelance.repository.project.ProjectRepository;
import com.example.freelance.repository.user.ClientProfileRepository;
import com.example.freelance.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ClientProfileRepository clientProfileRepository;
    @Mock private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserPrincipal principal = mock(UserPrincipal.class);

        when(principal.getId()).thenReturn(USER_ID);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createProject_WhenValidRequest_ShouldReturnResponse() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setTitle("New Project");
        request.setDescription("Desc");
        request.setBudgetMin(new BigDecimal("100"));
        request.setBudgetMax(new BigDecimal("200"));
        request.setCurrency("USD");
        request.setTagNames(Collections.emptyList());

        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setId(10L);

        when(clientProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(clientProfile));

        Project savedProject = new Project();
        savedProject.setId(100L);
        savedProject.setTitle(request.getTitle());
        savedProject.setStatus(ProjectStatus.DRAFT);

        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        ProjectResponse expectedResponse = ProjectResponse.builder()
                .id(100L)
                .title(request.getTitle())
                .status(ProjectStatus.DRAFT)
                .build();

        when(projectMapper.toResponse(savedProject)).thenReturn(expectedResponse);

        ProjectResponse response = projectService.createProject(request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo(ProjectStatus.DRAFT);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProject_WhenBudgetInvalid_ShouldThrowBadRequestException() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setBudgetMin(new BigDecimal("500"));
        request.setBudgetMax(new BigDecimal("100")); // Min > Max

        when(clientProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(new ClientProfile()));

        assertThrows(BadRequestException.class, () -> projectService.createProject(request));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void publishProject_WhenStatusDraft_ShouldUpdateToOpen() {
        Long projectId = 55L;
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(ProjectStatus.DRAFT);
        project.setTitle("Test Project");

        when(projectRepository.findByIdAndClientId(projectId, USER_ID))
                .thenReturn(Optional.of(project));

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(projectMapper.toResponse(any(Project.class))).thenReturn(new ProjectResponse());

        projectService.publishProject(projectId);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.OPEN);
        verify(projectRepository).save(project);
    }

    @Test
    void updateProject_WhenStatusCompleted_ShouldThrowBadRequestException() {
        Long projectId = 55L;
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(ProjectStatus.COMPLETED); // Вже завершено

        when(projectRepository.findByIdAndClientId(projectId, USER_ID))
                .thenReturn(Optional.of(project));

        assertThrows(BadRequestException.class, () -> projectService.updateProject(projectId, null));
    }
}