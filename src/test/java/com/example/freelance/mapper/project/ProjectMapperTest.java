package com.example.freelance.mapper.project;

import com.example.freelance.domain.project.Category;
import com.example.freelance.domain.project.Project;
import com.example.freelance.domain.project.ProjectStatus;
import com.example.freelance.domain.project.Tag;
import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.domain.user.User;
import com.example.freelance.dto.project.ProjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {ProjectMapperImpl.class})
class ProjectMapperTest {

    @Autowired
    private ProjectMapper projectMapper;

    @Test
    void toResponse_WhenProjectIsValid_ShouldReturnFullResponse() {
        Project project = getProject();

        ProjectResponse response = projectMapper.toResponse(project);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTitle()).isEqualTo("Test Project");
        assertThat(response.getClientId()).isEqualTo(1L);
        assertThat(response.getClientEmail()).isEqualTo("client@test.com");
        assertThat(response.getCategoryId()).isEqualTo(5L);
        assertThat(response.getCategoryName()).isEqualTo("Web Development");
        assertThat(response.getTagNames()).containsExactly("Java", "Spring");
        assertThat(response.getStatus()).isEqualTo(ProjectStatus.OPEN);
        assertThat(response.getDeadline()).isEqualTo("2024-12-31T00:00:00Z");
    }

    private static Project getProject() {
        User user = new User();
        user.setId(10L);
        user.setEmail("client@test.com");

        ClientProfile client = new ClientProfile();
        client.setId(1L);
        client.setUser(user);

        Category category = new Category();
        category.setId(5L);
        category.setName("Web Development");

        Tag tag1 = new Tag();
        tag1.setName("Java");
        Tag tag2 = new Tag();
        tag2.setName("Spring");

        Project project = new Project();
        project.setId(100L);
        project.setTitle("Test Project");
        project.setDescription("Description");
        project.setBudgetMin(new BigDecimal("100.00"));
        project.setBudgetMax(new BigDecimal("200.00"));
        project.setCurrency("USD");
        project.setStatus(ProjectStatus.OPEN);
        project.setDeadline(Instant.parse("2024-12-31T00:00:00Z"));
        project.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        project.setClient(client);
        project.setCategory(category);
        project.setTags(List.of(tag1, tag2));
        return project;
    }

    @Test
    void toResponse_WhenCategoryIsNull_ShouldReturnResponseWithNullCategoryFields() {
        User user = new User();
        user.setId(10L);
        user.setEmail("client@test.com");

        ClientProfile client = new ClientProfile();
        client.setId(1L);
        client.setUser(user);

        Project project = new Project();
        project.setId(100L);
        project.setClient(client);
        project.setCategory(null);

        ProjectResponse response = projectMapper.toResponse(project);

        assertThat(response.getCategoryId()).isNull();
        assertThat(response.getCategoryName()).isNull();
    }

    @Test
    void toResponse_WhenTagsAreNull_ShouldReturnEmptyTagList() {
        User user = new User();
        user.setId(10L);
        user.setEmail("client@test.com");

        ClientProfile client = new ClientProfile();
        client.setId(1L);
        client.setUser(user);

        Project project = new Project();
        project.setId(100L);
        project.setClient(client);
        project.setTags(null);

        ProjectResponse response = projectMapper.toResponse(project);

        assertThat(response.getTagNames()).isEmpty();
    }
}