package com.example.freelance.dto.user;

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
public class FreelancerProfileResponse {
    private Long id;
    private Long userId;
    private String email;
    private String displayName;
    private String bio;
    private List<String> skills;
    private BigDecimal hourlyRate;
    private String currency;
    private BigDecimal rating;
    private Integer completedProjectsCount;
    private Instant createdAt;
    private Instant updatedAt;
}

