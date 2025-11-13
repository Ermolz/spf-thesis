package com.example.freelance.dto.assignment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAssignmentRequest {
    private Instant startDate;
    private Instant endDate;
}

