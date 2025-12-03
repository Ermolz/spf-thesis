package com.example.freelance.dto.proposal;

import jakarta.validation.constraints.Digits;
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
public class CreateProposalRequest {
    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Cover letter is required")
    @Size(max = 5000, message = "Cover letter must not exceed 5000 characters")
    private String coverLetter;

    @NotNull(message = "Bid amount is required")
    @Positive(message = "Bid amount must be positive")
    @Digits(integer = 10, fraction = 2, message = "Bid amount can have at most two decimal places and must not exceed 9999999999.99")
    private BigDecimal bidAmount;

    @Positive(message = "Estimated duration must be positive")
    private Integer estimatedDuration;
}

