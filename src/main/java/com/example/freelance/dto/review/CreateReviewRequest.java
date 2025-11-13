package com.example.freelance.dto.review;

import com.example.freelance.domain.review.ReviewType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {
    @NotNull(message = "Assignment ID is required")
    private Long assignmentId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private BigDecimal rating;

    @Size(max = 5000, message = "Comment must not exceed 5000 characters")
    private String comment;

    @NotNull(message = "Review type is required")
    private ReviewType reviewType;
}

