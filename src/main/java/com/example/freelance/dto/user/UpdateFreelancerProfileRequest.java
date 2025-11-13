package com.example.freelance.dto.user;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFreelancerProfileRequest {
    @Size(max = 255, message = "Display name must not exceed 255 characters")
    private String displayName;

    @Size(max = 5000, message = "Bio must not exceed 5000 characters")
    private String bio;

    private List<String> skills;

    @Positive(message = "Hourly rate must be positive")
    private BigDecimal hourlyRate;

    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;
}

