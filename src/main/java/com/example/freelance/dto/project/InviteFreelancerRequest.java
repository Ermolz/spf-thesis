package com.example.freelance.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteFreelancerRequest {
    @NotNull(message = "Freelancer ID is required")
    private Long freelancerId;

    @NotBlank(message = "Invitation message is required")
    @Size(max = 2000, message = "Invitation message must not exceed 2000 characters")
    private String message;

    @Positive(message = "Suggested bid amount must be positive")
    private BigDecimal suggestedBidAmount;

    @Positive(message = "Estimated duration must be positive")
    private Integer estimatedDuration;
}

