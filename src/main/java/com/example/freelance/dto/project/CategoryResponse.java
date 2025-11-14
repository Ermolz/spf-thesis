package com.example.freelance.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing category information")
public class CategoryResponse {
    @Schema(description = "Category unique identifier", example = "1")
    private Long id;

    @Schema(description = "Category name", example = "Web Development")
    private String name;

    @Schema(description = "Category description", example = "Projects related to web development and design")
    private String description;

    @Schema(description = "Category creation timestamp", example = "2024-01-15T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2024-01-15T10:30:00Z")
    private Instant updatedAt;
}

