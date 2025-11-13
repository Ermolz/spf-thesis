package com.example.freelance.dto.proposal;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProposalRequest {
    @Size(max = 5000, message = "Cover letter must not exceed 5000 characters")
    private String coverLetter;

    @Positive(message = "Bid amount must be positive")
    private BigDecimal bidAmount;

    @Positive(message = "Estimated duration must be positive")
    private Integer estimatedDuration;
}

