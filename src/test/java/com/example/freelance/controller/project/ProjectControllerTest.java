package com.example.freelance.controller.project;

import com.example.freelance.domain.project.ProjectStatus;
import com.example.freelance.dto.project.CreateProjectRequest;
import com.example.freelance.dto.project.ProjectResponse;
import com.example.freelance.service.project.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @Test
    @WithMockUser(username = "client@test.com", roles = "CLIENT")
    void createProject_WhenClient_ShouldReturnCreated() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest(
                "New Project", "Desc", new BigDecimal("100"), new BigDecimal("200"),
                "USD", 1L, List.of("Java"), Instant.now()
        );

        ProjectResponse response = ProjectResponse.builder()
                .id(10L)
                .title("New Project")
                .status(ProjectStatus.DRAFT)
                .build();

        when(projectService.createProject(any(CreateProjectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    @WithMockUser(username = "freelancer@test.com", roles = "FREELANCER")
    void createProject_WhenFreelancer_ShouldReturnForbidden() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest(
                "New Project", "Desc", new BigDecimal("100"), new BigDecimal("200"),
                "USD", 1L, List.of("Java"), Instant.now()
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}