package com.example.freelance.dto.review;

import com.example.freelance.domain.review.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long authorId;
    private String authorEmail;
    private Long targetFreelancerId;
    private String targetFreelancerDisplayName;
    private Long targetClientId;
    private String targetClientEmail;
    private Long assignmentId;
    private String assignmentProjectTitle;
    private BigDecimal rating;
    private String comment;
    private ReviewType reviewType;
    private Instant createdAt;
    private Instant updatedAt;
}

