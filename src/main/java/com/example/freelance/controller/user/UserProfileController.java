package com.example.freelance.controller.user;

import com.example.freelance.common.dto.ApiResponse;
import com.example.freelance.common.util.ResponseUtil;
import com.example.freelance.dto.user.ClientProfileResponse;
import com.example.freelance.dto.user.FreelancerProfileResponse;
import com.example.freelance.dto.user.UpdateClientProfileRequest;
import com.example.freelance.dto.user.UpdateFreelancerProfileRequest;
import com.example.freelance.service.user.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "User Profiles", description = "User profile management endpoints. Freelancers and clients can view and update their profiles.")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
public class UserProfileController {
    private final UserProfileService userProfileService;

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get current freelancer profile",
            description = "Retrieves the profile of the authenticated freelancer."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only freelancers can view their profile"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Profile not found")
    })
    @GetMapping("/freelancer/me")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<FreelancerProfileResponse>> getMyFreelancerProfile() {
        FreelancerProfileResponse response = userProfileService.getMyFreelancerProfile();
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get freelancer profile by user ID",
            description = "Retrieves a freelancer profile by user ID. Available to all authenticated users.",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "userId", description = "User unique identifier", required = true, example = "1")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Profile not found")
    })
    @GetMapping("/freelancer/{userId}")
    public ResponseEntity<ApiResponse<FreelancerProfileResponse>> getFreelancerProfile(@PathVariable Long userId) {
        FreelancerProfileResponse response = userProfileService.getFreelancerProfile(userId);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Update freelancer profile",
            description = """
                    Updates the profile of the authenticated freelancer.
                    
                    **Updateable Fields:**
                    - displayName: Public display name
                    - bio: Professional biography
                    - skills: List of skills (e.g., ["Java", "Spring Boot", "PostgreSQL"])
                    - hourlyRate: Hourly rate for services
                    - currency: Currency code (e.g., USD, EUR)
                    
                    **Note:**
                    - Rating and completedProjectsCount are automatically calculated
                    - All fields are optional - only provided fields will be updated
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Profile update details",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UpdateFreelancerProfileRequest.class)
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile successfully updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only freelancers can update their profile")
    })
    @PutMapping("/freelancer/me")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<FreelancerProfileResponse>> updateFreelancerProfile(
            @Valid @RequestBody UpdateFreelancerProfileRequest request) {
        FreelancerProfileResponse response = userProfileService.updateFreelancerProfile(request);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get current client profile",
            description = "Retrieves the profile of the authenticated client."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only clients can view their profile"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Profile not found")
    })
    @GetMapping("/client/me")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ClientProfileResponse>> getMyClientProfile() {
        ClientProfileResponse response = userProfileService.getMyClientProfile();
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get client profile by user ID",
            description = "Retrieves a client profile by user ID. Available to all authenticated users.",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "userId", description = "User unique identifier", required = true, example = "1")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Profile not found")
    })
    @GetMapping("/client/{userId}")
    public ResponseEntity<ApiResponse<ClientProfileResponse>> getClientProfile(@PathVariable Long userId) {
        ClientProfileResponse response = userProfileService.getClientProfile(userId);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Update client profile",
            description = """
                    Updates the profile of the authenticated client.
                    
                    **Updateable Fields:**
                    - companyName: Company or organization name
                    - bio: Company description or bio
                    
                    **Note:**
                    - totalSpent and rating are automatically calculated
                    - All fields are optional - only provided fields will be updated
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Profile update details",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UpdateClientProfileRequest.class)
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile successfully updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Only clients can update their profile")
    })
    @PutMapping("/client/me")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ClientProfileResponse>> updateClientProfile(
            @Valid @RequestBody UpdateClientProfileRequest request) {
        ClientProfileResponse response = userProfileService.updateClientProfile(request);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }
}

