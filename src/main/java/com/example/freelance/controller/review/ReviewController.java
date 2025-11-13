package com.example.freelance.controller.review;

import com.example.freelance.common.dto.ApiResponse;
import com.example.freelance.common.util.ResponseUtil;
import com.example.freelance.dto.review.CreateReviewRequest;
import com.example.freelance.dto.review.ReviewResponse;
import com.example.freelance.service.review.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Reviews", description = "Review management endpoints. Allows clients and freelancers to rate and review each other after assignment completion.")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
public class ReviewController {
    private final ReviewService reviewService;

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create a review",
            description = """
                    Creates a review for a completed assignment. Both clients and freelancers can create reviews.
                    
                    **Review Types:**
                    - CLIENT_TO_FREELANCER: Client reviews the freelancer's work
                    - FREELANCER_TO_CLIENT: Freelancer reviews the client
                    
                    **Review Rules:**
                    - Assignment must be COMPLETED
                    - Can only create one review per assignment per type
                    - Rating must be between 1 and 5
                    - Review automatically updates profile ratings
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Review creation details",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CreateReviewRequest.class)
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Review successfully created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Assignment not completed or duplicate review"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to review this assignment"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(@Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.successWithTimestamp(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get freelancer reviews",
            description = """
                    Retrieves paginated list of reviews for a specific freelancer.
                    
                    **Pagination:**
                    - Default page size: 20
                    - Default sort: createdAt DESC (newest first)
                    """,
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "freelancerId", description = "Freelancer profile ID", required = true, example = "1"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "Page size", example = "20"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "Sort field and direction", example = "createdAt,desc")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Freelancer not found")
    })
    @GetMapping("/freelancer/{freelancerId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getFreelancerReviews(
            @PathVariable Long freelancerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> response = reviewService.getFreelancerReviews(freelancerId, pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get client reviews",
            description = """
                    Retrieves paginated list of reviews for a specific client.
                    
                    **Pagination:**
                    - Default page size: 20
                    - Default sort: createdAt DESC (newest first)
                    """,
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "clientId", description = "Client profile ID", required = true, example = "1"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "Page size", example = "20"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "Sort field and direction", example = "createdAt,desc")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client not found")
    })
    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getClientReviews(
            @PathVariable Long clientId,
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> response = reviewService.getClientReviews(clientId, pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get assignment reviews",
            description = """
                    Retrieves paginated list of reviews for a specific assignment.
                    
                    **Pagination:**
                    - Default page size: 20
                    - Default sort: createdAt DESC (newest first)
                    """,
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "assignmentId", description = "Assignment unique identifier", required = true, example = "1"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "Page size", example = "20"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "Sort field and direction", example = "createdAt,desc")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getAssignmentReviews(
            @PathVariable Long assignmentId,
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> response = reviewService.getAssignmentReviews(assignmentId, pageable);
        return ResponseEntity.ok(ResponseUtil.success(response));
    }
}

