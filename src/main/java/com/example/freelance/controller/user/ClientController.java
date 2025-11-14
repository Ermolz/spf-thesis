package com.example.freelance.controller.user;

import com.example.freelance.common.dto.ApiResponse;
import com.example.freelance.common.util.ResponseUtil;
import com.example.freelance.dto.user.FreelancerProfileResponse;
import com.example.freelance.service.user.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Client-specific endpoints for managing freelancer relationships and invitations.")
@SecurityRequirement(name = "Bearer Authentication")
public class ClientController {
    private final ClientService clientService;

    @Operation(
            summary = "Get verified freelancers",
            description = """
                    Retrieves paginated list of freelancers with whom the authenticated client has completed assignments.
                    These are freelancers that the client has worked with before and can re-invite to new projects.
                    
                    **Pagination:**
                    - Default page size: 20
                    - Default sort: completedAt DESC (most recent first)
                    """,
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "20"),
                    @Parameter(name = "sort", description = "Sort field and direction", example = "completedAt,desc")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verified freelancers retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only clients can view verified freelancers")
    })
    @GetMapping("/me/freelancers")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Page<FreelancerProfileResponse>>> getVerifiedFreelancers(
            @PageableDefault(size = 20, sort = "completedAt", 
                           direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<FreelancerProfileResponse> response = clientService.getVerifiedFreelancers(pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }
}

