package com.example.freelance.dto.assignment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssignmentRequest {
    @NotNull(message = "Proposal ID is required")
    private Long proposalId;

    @NotNull(message = "Start date is required")
    private Instant startDate;

    private Instant endDate;
}

