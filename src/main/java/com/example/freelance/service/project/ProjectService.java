package com.example.freelance.service.project;

import com.example.freelance.common.exception.BadRequestException;
import com.example.freelance.common.exception.ForbiddenException;
import com.example.freelance.common.exception.NotFoundException;
import com.example.freelance.domain.project.Category;
import com.example.freelance.domain.project.Project;
import com.example.freelance.domain.project.ProjectStatus;
import com.example.freelance.domain.project.Tag;
import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.domain.user.Role;
import com.example.freelance.dto.project.CreateProjectRequest;
import com.example.freelance.dto.project.ProjectResponse;
import com.example.freelance.dto.project.SearchProjectsRequest;
import com.example.freelance.dto.project.SearchProjectsParams;
import com.example.freelance.dto.project.UpdateProjectRequest;
import com.example.freelance.mapper.project.ProjectMapper;
import com.example.freelance.repository.project.CategoryRepository;
import com.example.freelance.repository.project.ProjectRepository;
import com.example.freelance.repository.project.TagRepository;
import com.example.freelance.repository.proposal.ProposalRepository;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private static final String PROJECT_RESOURCE_NAME = "Project";
    private final ProjectRepository projectRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ProposalRepository proposalRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final ProjectMapper projectMapper;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        UserPrincipal userPrincipal = getCurrentUser();
        ClientProfile client = getClientProfile(userPrincipal.getId());

        validateBudget(request.getBudgetMin(), request.getBudgetMax());

        Project project = new Project();
        project.setClient(client);
        project.setTitle(request.getTitle() != null ? request.getTitle().trim() : null);
        project.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        project.setBudgetMin(request.getBudgetMin());
        project.setBudgetMax(request.getBudgetMax());
        project.setCurrency(request.getCurrency() != null ? request.getCurrency().toUpperCase().trim() : null);
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
                .orElseThrow(() -> new NotFoundException(PROJECT_RESOURCE_NAME, projectId.toString()));

        if (project.getStatus() == ProjectStatus.COMPLETED || project.getStatus() == ProjectStatus.CANCELLED) {
            throw new BadRequestException("Cannot update completed or cancelled project", "PROJECT_ALREADY_FINALIZED");
        }

        if (request.getTitle() != null) {
            project.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription().trim());
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
                .orElseThrow(() -> new NotFoundException(PROJECT_RESOURCE_NAME, projectId.toString()));
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
            Instant minDeadline,
            Instant maxDeadline,
            Pageable pageable) {
        SearchProjectsParams params = SearchProjectsParams.builder()
                .status(status)
                .categoryId(categoryId)
                .minBudget(minBudget)
                .maxBudget(maxBudget)
                .tagIds(tagIds)
                .minDeadline(minDeadline)
                .maxDeadline(maxDeadline)
                .build();
        
        return executeSearchProjects(params, pageable);
    }

    private Page<ProjectResponse> executeSearchProjects(SearchProjectsParams params, Pageable pageable) {
        UserPrincipal userPrincipal = getCurrentUser();
        Role userRole = userPrincipal.getRole();
        
        ProjectStatus searchStatus = params.getStatus();
        if (searchStatus == ProjectStatus.DRAFT && userRole != Role.CLIENT && userRole != Role.ADMIN) {
            searchStatus = ProjectStatus.OPEN;
        }

        List<Long> tagIdsParam = (params.getTagIds() == null || params.getTagIds().isEmpty()) ? null : params.getTagIds();
        Long categoryIdParam = (params.getCategoryId() == null || params.getCategoryId() <= 0) ? null : params.getCategoryId();
        
        // Используем крайние значения для deadline, чтобы PostgreSQL мог определить тип параметра
        // Если параметр null, используем разумные крайние значения в пределах диапазона PostgreSQL timestamp
        Instant minDeadlineParam = params.getMinDeadline() != null 
                ? params.getMinDeadline() 
                : Instant.ofEpochSecond(0); // 1970-01-01T00:00:00Z
        Instant maxDeadlineParam = params.getMaxDeadline() != null 
                ? params.getMaxDeadline() 
                : Instant.parse("9999-12-31T23:59:59.999999Z"); // Максимальная дата в пределах PostgreSQL timestamp

        Page<Project> projects = projectRepository.searchProjects(
                searchStatus,
                categoryIdParam,
                params.getMinBudget(),
                params.getMaxBudget(),
                tagIdsParam,
                minDeadlineParam,
                maxDeadlineParam,
                pageable
        );

        return projects.map(this::mapToResponse);
    }

    @Transactional
    public void deleteProject(Long projectId) {
        UserPrincipal userPrincipal = getCurrentUser();
        Project project = projectRepository.findByIdAndClientId(projectId, userPrincipal.getId())
                .orElseThrow(() -> new NotFoundException(PROJECT_RESOURCE_NAME, projectId.toString()));

        if (project.getStatus() == ProjectStatus.IN_PROGRESS) {
            throw new BadRequestException("Cannot delete project in progress", "PROJECT_IN_PROGRESS");
        }

        proposalRepository.deleteByProjectId(projectId);
        
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectResponse publishProject(Long projectId) {
        UserPrincipal userPrincipal = getCurrentUser();
        Project project = projectRepository.findByIdAndClientId(projectId, userPrincipal.getId())
                .orElseThrow(() -> new NotFoundException(PROJECT_RESOURCE_NAME, projectId.toString()));

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

