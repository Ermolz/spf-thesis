package com.example.freelance.controller.proposal;

import com.example.freelance.common.dto.ApiResponse;
import com.example.freelance.common.util.ResponseUtil;
import com.example.freelance.dto.proposal.CreateProposalRequest;
import com.example.freelance.dto.proposal.ProposalResponse;
import com.example.freelance.dto.proposal.UpdateProposalRequest;
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
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
@Tag(name = "Proposals", description = "Proposal management endpoints. Freelancers can submit and manage proposals. Clients can view, accept, or reject proposals.")
@SecurityRequirement(name = "Bearer Authentication")
public class ProposalController {
    private final ProposalService proposalService;

    @Operation(
            summary = "Create a new proposal",
            description = """
                    Creates a new proposal for an open project. Only FREELANCER role can create proposals.
                    
                    **Proposal Creation Process:**
                    1. Validates project exists and is OPEN
                    2. Checks freelancer hasn't already submitted a proposal for this project
                    3. Creates proposal with PENDING status
                    4. Associates proposal with authenticated freelancer
                    
                    **Proposal Requirements:**
                    - Project must be OPEN
                    - Freelancer can only submit one proposal per project
                    - Bid amount should be within project budget range (recommended)
                    - Cover letter should explain why the freelancer is suitable
                    
                    **Proposal Status:**
                    - PENDING: Awaiting client decision
                    - ACCEPTED: Client accepted the proposal
                    - REJECTED: Client rejected the proposal
                    - WITHDRAWN: Freelancer withdrew the proposal
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Proposal creation details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateProposalRequest.class),
                            examples = @ExampleObject(
                                    name = "Proposal Example",
                                    value = """
                                            {
                                              "projectId": 1,
                                              "coverLetter": "I have 5+ years of experience in React and Node.js development. I've built similar e-commerce platforms and can deliver high-quality code within your timeline.",
                                              "bidAmount": 7500.00,
                                              "estimatedDuration": 30
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Proposal successfully created",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation error or business rule violation",
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
                                                          "message": "Bid amount must be positive",
                                                          "field": "bidAmount",
                                                          "rejectedValue": -100,
                                                          "status": 400,
                                                          "path": "/api/proposals",
                                                          "timestamp": "2024-01-15T10:30:00Z"
                                                        }
                                                      ]
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Duplicate Proposal",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "errors": [
                                                        {
                                                          "code": "PROPOSAL_ALREADY_EXISTS",
                                                          "message": "You have already submitted a proposal for this project",
                                                          "status": 409,
                                                          "path": "/api/proposals",
                                                          "timestamp": "2024-01-15T10:30:00Z"
                                                        }
                                                      ]
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Project Not Published",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "errors": [
                                                        {
                                                          "code": "PROJECT_NOT_OPEN",
                                                          "message": "Cannot submit proposal for project that is not open",
                                                          "status": 400,
                                                          "path": "/api/proposals",
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
                    description = "Forbidden - Only freelancers can create proposals",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Project not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<ProposalResponse>> createProposal(@Valid @RequestBody CreateProposalRequest request) {
        ProposalResponse response = proposalService.createProposal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.successWithTimestamp(response));
    }

    @Operation(
            summary = "Update a proposal",
            description = """
                    Updates an existing proposal. Only the proposal owner (freelancer) can update.
                    
                    **Update Rules:**
                    - Can only update proposals in PENDING status
                    - Cannot update accepted, rejected, or withdrawn proposals
                    - All fields are optional - only provided fields will be updated
                    """,
            parameters = {
                    @Parameter(name = "id", description = "Proposal unique identifier", required = true, example = "1")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Proposal successfully updated"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Cannot update proposal that is not pending"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Not the proposal owner"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Proposal not found"
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<ProposalResponse>> updateProposal(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProposalRequest request) {
        ProposalResponse response = proposalService.updateProposal(id, request);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @Operation(
            summary = "Get proposal by ID",
            description = """
                    Retrieves a single proposal by its ID.
                    
                    **Visibility:**
                    - Freelancers can view their own proposals
                    - Clients can view proposals for their projects
                    """,
            parameters = {
                    @Parameter(name = "id", description = "Proposal unique identifier", required = true, example = "1")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Proposal found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions to view this proposal"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProposalResponse>> getProposalById(@PathVariable Long id) {
        ProposalResponse response = proposalService.getProposalById(id);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @Operation(
            summary = "Get current freelancer's proposals",
            description = """
                    Retrieves paginated list of proposals submitted by the authenticated freelancer.
                    
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Proposals retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only freelancers can view their proposals"
            )
    })
    @GetMapping("/my")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<Page<ProposalResponse>>> getMyProposals(
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<ProposalResponse> response = proposalService.getMyProposals(pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @Operation(
            summary = "Get proposals for a project",
            description = """
                    Retrieves paginated list of proposals for a specific project.
                    Only the project owner (client) can view proposals.
                    
                    **Pagination:**
                    - Default page size: 20
                    - Default sort: createdAt DESC (newest first)
                    """,
            parameters = {
                    @Parameter(name = "projectId", description = "Project unique identifier", required = true, example = "1"),
                    @Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "20"),
                    @Parameter(name = "sort", description = "Sort field and direction", example = "createdAt,desc")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Proposals retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Not the project owner"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            )
    })
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Page<ProposalResponse>>> getProjectProposals(
            @PathVariable Long projectId,
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<ProposalResponse> response = proposalService.getProjectProposals(projectId, pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @Operation(
            summary = "Accept a proposal",
            description = """
                    Accepts a proposal, changing its status to ACCEPTED.
                    Only the project owner (client) can accept proposals.
                    
                    **Acceptance Process:**
                    1. Validates proposal is in PENDING status
                    2. Validates client is the project owner
                    3. Changes proposal status to ACCEPTED
                    4. Rejects all other pending proposals for the same project
                    5. Client should then create an Assignment from this proposal
                    
                    **Important:**
                    - Only one proposal can be accepted per project
                    - Accepting a proposal automatically rejects other pending proposals
                    - After acceptance, create an Assignment using `/api/assignments` endpoint
                    """,
            parameters = {
                    @Parameter(name = "id", description = "Proposal unique identifier", required = true, example = "1")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Proposal successfully accepted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Proposal cannot be accepted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not the project owner"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ProposalResponse>> acceptProposal(@PathVariable Long id) {
        ProposalResponse response = proposalService.acceptProposal(id);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @Operation(
            summary = "Reject a proposal",
            description = """
                    Rejects a proposal, changing its status to REJECTED.
                    Only the project owner (client) can reject proposals.
                    
                    **Rejection Process:**
                    1. Validates proposal is in PENDING status
                    2. Validates client is the project owner
                    3. Changes proposal status to REJECTED
                    
                    **Note:**
                    - Rejected proposals cannot be accepted later
                    - Freelancer can submit a new proposal if needed
                    """,
            parameters = {
                    @Parameter(name = "id", description = "Proposal unique identifier", required = true, example = "1")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Proposal successfully rejected"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Proposal cannot be rejected"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not the project owner"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ProposalResponse>> rejectProposal(@PathVariable Long id) {
        ProposalResponse response = proposalService.rejectProposal(id);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @Operation(
            summary = "Withdraw a proposal",
            description = """
                    Withdraws a proposal, changing its status to WITHDRAWN.
                    Only the proposal owner (freelancer) can withdraw.
                    
                    **Withdrawal Process:**
                    1. Validates proposal is in PENDING status
                    2. Validates freelancer is the proposal owner
                    3. Changes proposal status to WITHDRAWN
                    
                    **Note:**
                    - Withdrawn proposals cannot be reactivated
                    - Freelancer can submit a new proposal if needed
                    """,
            parameters = {
                    @Parameter(name = "id", description = "Proposal unique identifier", required = true, example = "1")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Proposal successfully withdrawn"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Proposal cannot be withdrawn"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not the proposal owner"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<ProposalResponse>> withdrawProposal(@PathVariable Long id) {
        ProposalResponse response = proposalService.withdrawProposal(id);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }
}
