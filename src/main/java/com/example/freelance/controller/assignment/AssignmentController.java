package com.example.freelance.controller.assignment;

import com.example.freelance.common.dto.ApiResponse;
import com.example.freelance.common.util.ResponseUtil;
import com.example.freelance.dto.assignment.AssignmentResponse;
import com.example.freelance.dto.assignment.CreateAssignmentRequest;
import com.example.freelance.dto.assignment.UpdateAssignmentRequest;
import com.example.freelance.service.assignment.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Tag(name = "Assignments", description = "Assignment/Contract management endpoints. Assignments are created from accepted proposals and represent active work contracts between clients and freelancers.")
@SecurityRequirement(name = "Bearer Authentication")
public class AssignmentController {
    private final AssignmentService assignmentService;

    @Operation(
            summary = "Create a new assignment",
            description = "Creates an assignment from an accepted proposal. Only CLIENT role can create assignments. The proposal must be in ACCEPTED status."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Assignment successfully created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Proposal not accepted or invalid data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only clients can create assignments"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(@Valid @RequestBody CreateAssignmentRequest request) {
        AssignmentResponse response = assignmentService.createAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.successWithTimestamp(response));
    }

    @Operation(
            summary = "Update an assignment",
            description = "Updates an assignment. Only the client or freelancer associated with the assignment can update it.",
            parameters = {
                    @Parameter(name = "id", description = "Assignment unique identifier", required = true, example = "1")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assignment successfully updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this assignment"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssignmentRequest request) {
        AssignmentResponse response = assignmentService.updateAssignment(id, request);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @Operation(
            summary = "Get assignment by ID",
            description = "Retrieves a single assignment by its ID. Only the client or freelancer associated with the assignment can view it.",
            parameters = {
                    @Parameter(name = "id", description = "Assignment unique identifier", required = true, example = "1")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assignment found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this assignment"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignmentById(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @Operation(
            summary = "Get assignment by project ID",
            description = "Retrieves the assignment for a specific project. Only the client or freelancer associated with the assignment can view it.",
            parameters = {
                    @Parameter(name = "projectId", description = "Project unique identifier", required = true, example = "1")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assignment found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this assignment"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignmentByProjectId(@PathVariable Long projectId) {
        AssignmentResponse response = assignmentService.getAssignmentByProjectId(projectId);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @Operation(
            summary = "Get freelancer's assignments",
            description = """
                    Retrieves paginated list of assignments for the authenticated freelancer.
                    
                    **Pagination:**
                    - Default page size: 20
                    - Default sort: createdAt DESC (newest first)
                    """,
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "20"),
                    @Parameter(name = "sort", description = "Sort field and direction", example = "createdAt,desc")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assignments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only freelancers can view their assignments")
    })
    @GetMapping("/my")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<Page<AssignmentResponse>>> getMyAssignments(
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<AssignmentResponse> response = assignmentService.getMyAssignments(pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @Operation(
            summary = "Get client's assignments",
            description = """
                    Retrieves paginated list of assignments for the authenticated client.
                    
                    **Pagination:**
                    - Default page size: 20
                    - Default sort: createdAt DESC (newest first)
                    """,
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "20"),
                    @Parameter(name = "sort", description = "Sort field and direction", example = "createdAt,desc")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assignments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only clients can view their assignments")
    })
    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Page<AssignmentResponse>>> getClientAssignments(
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<AssignmentResponse> response = assignmentService.getClientAssignments(pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @Operation(
            summary = "Complete an assignment",
            description = """
                    Marks an assignment as completed. Both client and freelancer can complete an assignment.
                    
                    **Completion Process:**
                    1. Validates assignment is in ACTIVE status
                    2. Changes status to COMPLETED
                    3. Assignment can no longer be modified
                    """,
            parameters = {
                    @Parameter(name = "id", description = "Assignment unique identifier", required = true, example = "1")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assignment successfully completed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Assignment cannot be completed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not authorized for this assignment"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<AssignmentResponse>> completeAssignment(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.completeAssignment(id);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @Operation(
            summary = "Cancel an assignment",
            description = """
                    Cancels an assignment. Only the client can cancel.
                    
                    **Cancellation Process:**
                    1. Validates assignment is in ACTIVE status
                    2. Changes status to CANCELLED
                    3. Assignment can no longer be modified
                    """,
            parameters = {
                    @Parameter(name = "id", description = "Assignment unique identifier", required = true, example = "1")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assignment successfully cancelled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Assignment cannot be cancelled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only clients can cancel assignments"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> cancelAssignment(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.cancelAssignment(id);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }
}

