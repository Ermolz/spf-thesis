package com.example.freelance.service.project;

import com.example.freelance.common.exception.BadRequestException;
import com.example.freelance.common.exception.ForbiddenException;
import com.example.freelance.common.exception.NotFoundException;
import com.example.freelance.domain.project.Category;
import com.example.freelance.domain.project.Project;
import com.example.freelance.domain.project.ProjectStatus;
import com.example.freelance.domain.project.Tag;
import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.dto.project.CreateProjectRequest;
import com.example.freelance.dto.project.ProjectResponse;
import com.example.freelance.dto.project.UpdateProjectRequest;
import com.example.freelance.mapper.project.ProjectMapper;
import com.example.freelance.repository.project.CategoryRepository;
import com.example.freelance.repository.project.ProjectRepository;
import com.example.freelance.repository.project.TagRepository;
import com.example.freelance.repository.user.ClientProfileRepository;
import com.example.freelance.common.util.MdcUtil;
import com.example.freelance.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final ProjectMapper projectMapper;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        ClientProfile client = getClientProfile(userPrincipal.getId());

        validateBudget(request.getBudgetMin(), request.getBudgetMax());

        Project project = new Project();
        project.setClient(client);
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setBudgetMin(request.getBudgetMin());
        project.setBudgetMax(request.getBudgetMax());
        project.setCurrency(request.getCurrency().toUpperCase());
        project.setDeadline(request.getDeadline());
        project.setStatus(ProjectStatus.DRAFT);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId().toString()));
            project.setCategory(category);
        }

        if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
            List<Tag> tags = getOrCreateTags(request.getTagNames());
            project.setTags(tags);
        }

        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        Project project = projectRepository.findByIdAndClientId(projectId, userPrincipal.getId())
                .orElseThrow(() -> new NotFoundException("Project", projectId.toString()));

        if (project.getStatus() == ProjectStatus.COMPLETED || project.getStatus() == ProjectStatus.CANCELLED) {
            throw new BadRequestException("Cannot update completed or cancelled project", "PROJECT_ALREADY_FINALIZED");
        }

        if (request.getTitle() != null) {
            project.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getBudgetMin() != null || request.getBudgetMax() != null) {
            BigDecimal budgetMin = request.getBudgetMin() != null ? request.getBudgetMin() : project.getBudgetMin();
            BigDecimal budgetMax = request.getBudgetMax() != null ? request.getBudgetMax() : project.getBudgetMax();
            validateBudget(budgetMin, budgetMax);
            project.setBudgetMin(budgetMin);
            project.setBudgetMax(budgetMax);
        }
        if (request.getCurrency() != null) {
            project.setCurrency(request.getCurrency().toUpperCase());
        }
        if (request.getDeadline() != null) {
            project.setDeadline(request.getDeadline());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId().toString()));
            project.setCategory(category);
        }
        if (request.getTagNames() != null) {
            List<Tag> tags = getOrCreateTags(request.getTagNames());
            project.setTags(tags);
        }
        if (request.getStatus() != null) {
            ProjectStatus oldStatus = project.getStatus();
            validateStatusTransition(project.getStatus(), request.getStatus());
            project.setStatus(request.getStatus());
            
            // Log status change
            MdcUtil.setUserId(userPrincipal.getId());
            MdcUtil.setOperation("UPDATE_PROJECT_STATUS");
            log.info("Project status changed: projectId={}, title={}, oldStatus={}, newStatus={}, changedBy={}", 
                    projectId, project.getTitle(), oldStatus, request.getStatus(), userPrincipal.getId());
            MdcUtil.clearCustomValues();
        }

        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project", projectId.toString()));
        return mapToResponse(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getMyProjects(Pageable pageable) {
        UserPrincipal userPrincipal = getCurrentUser();
        ClientProfile client = getClientProfile(userPrincipal.getId());

        Page<Project> projects = projectRepository.findByClientId(client.getId(), pageable);
        return projects.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> searchProjects(
            ProjectStatus status,
            Long categoryId,
            BigDecimal minBudget,
            BigDecimal maxBudget,
            List<Long> tagIds,
            Pageable pageable) {
        ProjectStatus searchStatus = status != null ? status : ProjectStatus.OPEN;

        Page<Project> projects;

        if (tagIds != null && !tagIds.isEmpty()) {
            projects = projectRepository.searchProjectsWithMultipleTags(
                    searchStatus,
                    categoryId,
                    minBudget,
                    maxBudget,
                    tagIds,
                    pageable
            );
        } else {
            Long tagId = tagIds != null && !tagIds.isEmpty() ? tagIds.get(0) : null;

            projects = projectRepository.searchProjects(
                    searchStatus,
                    categoryId,
                    minBudget,
                    maxBudget,
                    tagId,
                    pageable
            );
        }

        return projects.map(this::mapToResponse);
    }

    @Transactional
    public void deleteProject(Long projectId) {
        UserPrincipal userPrincipal = getCurrentUser();
        Project project = projectRepository.findByIdAndClientId(projectId, userPrincipal.getId())
                .orElseThrow(() -> new NotFoundException("Project", projectId.toString()));

        if (project.getStatus() == ProjectStatus.IN_PROGRESS) {
            throw new BadRequestException("Cannot delete project in progress", "PROJECT_IN_PROGRESS");
        }

        projectRepository.delete(project);
    }

    @Transactional
    public ProjectResponse publishProject(Long projectId) {
        UserPrincipal userPrincipal = getCurrentUser();
        Project project = projectRepository.findByIdAndClientId(projectId, userPrincipal.getId())
                .orElseThrow(() -> new NotFoundException("Project", projectId.toString()));

        if (project.getStatus() != ProjectStatus.DRAFT) {
            throw new BadRequestException("Only draft projects can be published", "INVALID_STATUS_TRANSITION");
        }

        MdcUtil.setUserId(userPrincipal.getId());
        MdcUtil.setOperation("PUBLISH_PROJECT");
        
        ProjectStatus oldStatus = project.getStatus();
        project.setStatus(ProjectStatus.OPEN);
        project = projectRepository.save(project);
        
        log.info("Project published: projectId={}, title={}, oldStatus={}, newStatus={}", 
                projectId, project.getTitle(), oldStatus, ProjectStatus.OPEN);
        MdcUtil.clearCustomValues();
        
        return mapToResponse(project);
    }

    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private ClientProfile getClientProfile(Long userId) {
        return clientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("Only clients can create projects", "CLIENT_PROFILE_REQUIRED"));
    }

    private void validateBudget(BigDecimal budgetMin, BigDecimal budgetMax) {
        if (budgetMin != null && budgetMax != null && budgetMin.compareTo(budgetMax) > 0) {
            throw new BadRequestException("Minimum budget cannot be greater than maximum budget", "INVALID_BUDGET_RANGE");
        }
    }

    private void validateStatusTransition(ProjectStatus currentStatus, ProjectStatus newStatus) {
        if (currentStatus == ProjectStatus.COMPLETED || currentStatus == ProjectStatus.CANCELLED) {
            throw new BadRequestException("Cannot change status of finalized project", "PROJECT_ALREADY_FINALIZED");
        }

        if (newStatus == ProjectStatus.DRAFT && currentStatus != ProjectStatus.DRAFT) {
            throw new BadRequestException("Cannot revert to draft status", "INVALID_STATUS_TRANSITION");
        }
    }

    private List<Tag> getOrCreateTags(List<String> tagNames) {
        Set<String> uniqueTagNames = new HashSet<>(tagNames);
        List<Tag> existingTags = tagRepository.findByNameIn(uniqueTagNames);
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        List<Tag> tags = new ArrayList<>(existingTags);

        for (String tagName : uniqueTagNames) {
            if (!existingTagNames.contains(tagName)) {
                Tag newTag = new Tag();
                newTag.setName(tagName);
                tags.add(tagRepository.save(newTag));
            }
        }

        return tags;
    }

    private ProjectResponse mapToResponse(Project project) {
        return projectMapper.toResponse(project);
    }
}

