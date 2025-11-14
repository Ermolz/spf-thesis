package com.example.freelance.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Request DTO for creating a new project")
public class CreateProjectRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(description = "Project title", example = "E-commerce Website Development", maxLength = 255)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    @Schema(description = "Detailed project description", example = "Looking for an experienced developer to build a modern e-commerce platform...", maxLength = 5000)
    private String description;

    @Positive(message = "Minimum budget must be positive")
    @Schema(description = "Minimum budget amount", example = "5000.00")
    private BigDecimal budgetMin;

    @Positive(message = "Maximum budget must be positive")
    @Schema(description = "Maximum budget amount", example = "10000.00")
    private BigDecimal budgetMax;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Schema(description = "Currency code (ISO 4217, 3 letters)", example = "USD", minLength = 3, maxLength = 3)
    private String currency;

    @Schema(description = "Category ID (optional)", example = "1")
    private Long categoryId;

    @Schema(description = "List of tag names (tags are created if they don't exist)", example = "[\"React\", \"Node.js\", \"PostgreSQL\"]")
    private List<String> tagNames;

    @Schema(description = "Project deadline (optional)", example = "2024-06-01T00:00:00Z")
    private Instant deadline;
}

