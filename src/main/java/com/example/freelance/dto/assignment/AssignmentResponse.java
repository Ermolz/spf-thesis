package com.example.freelance.dto.assignment;

import com.example.freelance.domain.assignment.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {
    private Long id;
    private Long projectId;
    private String projectTitle;
    private Long freelancerId;
    private String freelancerEmail;
    private String freelancerDisplayName;
    private Long clientId;
    private String clientEmail;
    private Long proposalId;
    private Instant startDate;
    private Instant endDate;
    private AssignmentStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}

