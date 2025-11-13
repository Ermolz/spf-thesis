package com.example.freelance.dto.project;

import com.example.freelance.domain.project.ProjectStatus;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectRequest {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Positive(message = "Minimum budget must be positive")
    private BigDecimal budgetMin;

    @Positive(message = "Maximum budget must be positive")
    private BigDecimal budgetMax;

    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    private Long categoryId;

    private List<String> tagNames;

    private Instant deadline;

    private ProjectStatus status;
}

