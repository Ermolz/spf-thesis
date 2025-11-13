package com.example.freelance.dto.proposal;

import com.example.freelance.domain.proposal.ProposalStatus;
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
public class ProposalResponse {
    private Long id;
    private Long projectId;
    private String projectTitle;
    private Long freelancerId;
    private String freelancerEmail;
    private String freelancerDisplayName;
    private String coverLetter;
    private BigDecimal bidAmount;
    private Integer estimatedDuration;
    private ProposalStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}

