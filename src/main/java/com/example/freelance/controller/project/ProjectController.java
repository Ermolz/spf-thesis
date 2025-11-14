package com.example.freelance.controller.project;

import com.example.freelance.common.dto.ApiResponse;
import com.example.freelance.common.util.ResponseUtil;
import com.example.freelance.domain.project.ProjectStatus;
import com.example.freelance.dto.project.CreateProjectRequest;
import com.example.freelance.dto.project.InviteFreelancerRequest;
import com.example.freelance.dto.project.ProjectResponse;
import com.example.freelance.dto.project.UpdateProjectRequest;
import com.example.freelance.dto.proposal.ProposalResponse;
import com.example.freelance.service.project.ProjectService;
import com.example.freelance.service.proposal.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management endpoints. Clients can create, update, and manage their projects. Freelancers can search and view published projects.")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectController {
    private final ProjectService projectService;
    private final ProposalService proposalService;

    @Operation(
            summary = "Create a new project",
            description = """
                    Creates a new project draft. Only CLIENT role can create projects.
                    
                    **Project Creation Process:**
                    1. Validates all required fields and constraints
                    2. Creates project with DRAFT status
                    3. Associates project with authenticated client
                    4. Creates or links categories and tags
                    
                    **Project Status:**
                    - Projects are created as DRAFT by default
                    - Use `/api/projects/{id}/publish` to change status to OPEN
                    - Only OPEN projects are visible in search results (by default)
                    
                    **Budget:**
                    - `budgetMin` and `budgetMax` define the budget range
                    - Both values must be positive
                    - Currency must be a valid 3-letter ISO code (e.g., USD, EUR)
                    
                    **Categories and Tags:**
                    - `categoryId` is optional but recommended for better project organization
                    - `tagNames` are created automatically if they don't exist
                    - Tags help freelancers find relevant projects
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Project creation details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateProjectRequest.class),
                            examples = @ExampleObject(
                                    name = "Web Development Project",
                                    value = """
                                            {
                                              "title": "E-commerce Website Development",
                                              "description": "Looking for an experienced developer to build a modern e-commerce platform with payment integration and admin dashboard.",
                                              "budgetMin": 5000.00,
                                              "budgetMax": 10000.00,
                                              "currency": "USD",
                                              "categoryId": 1,
                                              "tagNames": ["React", "Node.js", "PostgreSQL", "Payment Integration"],
                                              "deadline": "2024-06-01T00:00:00Z"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Project successfully created",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Created Project",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "id": 1,
                                                "clientId": 5,
                                                "clientEmail": "client@example.com",
                                                "title": "E-commerce Website Development",
                                                "description": "Looking for an experienced developer...",
                                                "budgetMin": 5000.00,
                                                "budgetMax": 10000.00,
                                                "currency": "USD",
                                                "categoryId": 1,
                                                "categoryName": "Web Development",
                                                "tagNames": ["React", "Node.js", "PostgreSQL"],
                                                "deadline": "2024-06-01T00:00:00Z",
                                                "status": "DRAFT",
                                                "createdAt": "2024-01-15T10:30:00Z",
                                                "updatedAt": "2024-01-15T10:30:00Z"
                                              },
                                              "metadata": {
                                                "timestamp": "2024-01-15T10:30:00Z"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation error or invalid data",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation Error",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "errors": [
                                                        {
                                                          "code": "VALIDATION_ERROR",
                                                          "message": "Title is required",
                                                          "field": "title",
                                                          "rejectedValue": "",
                                                          "status": 400,
                                                          "path": "/api/projects",
                                                          "timestamp": "2024-01-15T10:30:00Z"
                                                        }
                                                      ]
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Category Not Found",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "errors": [
                                                        {
                                                          "code": "RESOURCE_NOT_FOUND",
                                                          "message": "Category not found",
                                                          "status": 404,
                                                          "path": "/api/projects",
                                                          "timestamp": "2024-01-15T10:30:00Z"
                                                        }
                                                      ]
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only clients can create projects",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.successWithTimestamp(response));
    }

    @Operation(
            summary = "Update an existing project",
            description = """
                    Updates an existing project. Only the project owner (client who created it) can update.
                    
                    **Update Rules:**
                    - Can only update projects in DRAFT status
                    - Cannot change project status through this endpoint (use publish endpoint)
                    - All fields are optional - only provided fields will be updated
                    - Cannot update project after it has been published and has active proposals
                    """,
            parameters = {
                    @Parameter(name = "id", description = "Project unique identifier", required = true, example = "1")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project successfully updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Cannot update published project with proposals"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not the project owner"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request) {
        ProjectResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @Operation(
            summary = "Get project by ID",
            description = """
                    Retrieves a single project by its ID. Available to all authenticated users.
                    
                    **Visibility:**
                    - DRAFT projects: Only visible to the project owner
                    - OPEN projects: Visible to all users
                    - CLOSED projects: Visible to all users
                    """,
            parameters = {
                    @Parameter(name = "id", description = "Project unique identifier", required = true, example = "1")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Trying to access draft project of another user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable Long id) {
        ProjectResponse response = projectService.getProjectById(id);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @Operation(
            summary = "Get current user's projects",
            description = """
                    Retrieves paginated list of projects created by the authenticated client.
                    
                    **Pagination:**
                    - Default page size: 20
                    - Default sort: createdAt DESC (newest first)
                    - Supports standard Spring Data pagination parameters
                    """,
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "20"),
                    @Parameter(name = "sort", description = "Sort field and direction", example = "createdAt,desc")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only clients can view their projects")
    })
    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getMyProjects(
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<ProjectResponse> response = projectService.getMyProjects(pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @Operation(
            summary = "Search projects",
            description = """
                    Searches for projects with optional filters. Available to all authenticated users.
                    By default, only OPEN projects are included in search results (unless status is specified).
                    
                    **Search Filters:**
                    - `status`: Filter by project status. Valid values: `DRAFT`, `OPEN`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `CLOSED`. Default: `OPEN` (if not specified)
                    - `categoryId`: Filter by category
                    - `minBudget`: Minimum budget amount
                    - `maxBudget`: Maximum budget amount
                    - `tagIds`: Filter by tag IDs (projects must have ALL specified tags)
                    
                    **Pagination:**
                    - Default page size: 20
                    - Default sort: createdAt DESC
                    """,
            parameters = {
                    @Parameter(name = "status", description = "Project status filter. Valid values: DRAFT, OPEN, IN_PROGRESS, COMPLETED, CANCELLED, CLOSED", example = "OPEN"),
                    @Parameter(name = "categoryId", description = "Category ID filter", example = "1"),
                    @Parameter(name = "minBudget", description = "Minimum budget filter", example = "1000.00"),
                    @Parameter(name = "maxBudget", description = "Maximum budget filter", example = "10000.00"),
                    @Parameter(name = "tagIds", description = "Tag IDs filter (projects must have all tags)", example = "[1, 2, 3]"),
                    @Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "20"),
                    @Parameter(name = "sort", description = "Sort field and direction", example = "createdAt,desc")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> searchProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) java.math.BigDecimal minBudget,
            @RequestParam(required = false) java.math.BigDecimal maxBudget,
            @RequestParam(required = false) java.util.List<Long> tagIds,
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<ProjectResponse> response = projectService.searchProjects(status, categoryId, minBudget, maxBudget, tagIds, pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @Operation(
            summary = "Delete a project",
            description = """
                    Deletes a project. Only the project owner can delete.
                    
                    **Deletion Rules:**
                    - Can only delete projects in DRAFT status
                    - Cannot delete projects with active proposals or assignments
                    - Deletion is permanent and cannot be undone
                    """,
            parameters = {
                    @Parameter(name = "id", description = "Project unique identifier", required = true, example = "1")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Project successfully deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Cannot delete project with active proposals"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not the project owner"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Publish a project",
            description = """
                    Publishes a project, making it visible to freelancers in search results.
                    Only the project owner can publish.
                    
                    **Publishing Process:**
                    1. Validates project is in DRAFT status
                    2. Changes status to OPEN
                    3. Project becomes visible in search results
                    4. Freelancers can now submit proposals
                    """,
            parameters = {
                    @Parameter(name = "id", description = "Project unique identifier", required = true, example = "1")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project successfully published"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Project cannot be published"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not the project owner"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ProjectResponse>> publishProject(@PathVariable Long id) {
        ProjectResponse response = projectService.publishProject(id);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @Operation(
            summary = "Invite freelancer to project",
            description = """
                    Invites a freelancer to submit a proposal for the project. Only the project owner (client) can invite freelancers.
                    
                    **Invitation Process:**
                    1. Validates project exists and client is the owner
                    2. Validates freelancer exists
                    3. If project is DRAFT, automatically publishes it to OPEN
                    4. Creates a proposal with PENDING status on behalf of the freelancer
                    5. Freelancer can then accept or modify the proposal
                    
                    **Note:**
                    - If project is DRAFT, it will be automatically published
                    - Creates a proposal that the freelancer can accept or modify
                    - Cannot invite if proposal already exists for this freelancer and project
                    """,
            parameters = {
                    @Parameter(name = "projectId", description = "Project unique identifier", required = true, example = "1")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Freelancer successfully invited"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Invalid project status or data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not the project owner"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project or freelancer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - Proposal already exists")
    })
    @PostMapping("/{projectId}/invite")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ProposalResponse>> inviteFreelancer(
            @PathVariable Long projectId,
            @Valid @RequestBody InviteFreelancerRequest request) {
        ProposalResponse response = proposalService.inviteFreelancer(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.successWithTimestamp(response));
    }
}
