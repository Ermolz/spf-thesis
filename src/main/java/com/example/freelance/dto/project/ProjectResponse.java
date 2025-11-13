package com.example.freelance.dto.project;

import com.example.freelance.domain.project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response DTO containing project information")
public class ProjectResponse {
    @Schema(description = "Project unique identifier", example = "1")
    private Long id;
    
    @Schema(description = "Client profile ID who created the project", example = "5")
    private Long clientId;
    
    @Schema(description = "Client email address", example = "client@example.com")
    private String clientEmail;
    
    @Schema(description = "Project title", example = "E-commerce Website Development")
    private String title;
    
    @Schema(description = "Project description", example = "Looking for an experienced developer...")
    private String description;
    
    @Schema(description = "Minimum budget", example = "5000.00")
    private BigDecimal budgetMin;
    
    @Schema(description = "Maximum budget", example = "10000.00")
    private BigDecimal budgetMax;
    
    @Schema(description = "Currency code", example = "USD")
    private String currency;
    
    @Schema(description = "Category ID", example = "1")
    private Long categoryId;
    
    @Schema(description = "Category name", example = "Web Development")
    private String categoryName;
    
    @Schema(description = "List of tag names", example = "[\"React\", \"Node.js\", \"PostgreSQL\"]")
    private List<String> tagNames;
    
    @Schema(description = "Project deadline", example = "2024-06-01T00:00:00Z")
    private Instant deadline;
    
    @Schema(description = "Project status", example = "OPEN", allowableValues = {"DRAFT", "OPEN", "IN_PROGRESS", "COMPLETED", "CANCELLED", "CLOSED"})
    private ProjectStatus status;
    
    @Schema(description = "Project creation timestamp", example = "2024-01-15T10:30:00Z")
    private Instant createdAt;
    
    @Schema(description = "Last update timestamp", example = "2024-01-15T10:30:00Z")
    private Instant updatedAt;
}

