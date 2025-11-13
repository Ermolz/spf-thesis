package com.example.freelance.dto.user;

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
public class ClientProfileResponse {
    private Long id;
    private Long userId;
    private String email;
    private String companyName;
    private String bio;
    private BigDecimal totalSpent;
    private BigDecimal rating;
    private Instant createdAt;
    private Instant updatedAt;
}

