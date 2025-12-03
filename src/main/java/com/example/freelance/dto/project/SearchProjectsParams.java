package com.example.freelance.dto.project;

import com.example.freelance.domain.project.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchProjectsParams {
    private ProjectStatus status;
    private Long categoryId;
    private BigDecimal minBudget;
    private BigDecimal maxBudget;
    private List<Long> tagIds;
    private Instant minDeadline;
    private Instant maxDeadline;
}

