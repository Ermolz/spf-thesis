package com.example.freelance.common.dto;

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
@Schema(description = "Additional metadata for API responses")
public class Metadata {
    
    @Schema(description = "Pagination information")
    private PaginationInfo pagination;
    
    @Schema(description = "Response timestamp", example = "2024-01-15T10:30:00Z")
    private Instant timestamp;
    
    @Schema(description = "Total count of items (for paginated responses)")
    private Long totalCount;
    
    @Schema(description = "Additional custom metadata")
    private java.util.Map<String, Object> custom;
    
    public static Metadata withPagination(PaginationInfo pagination) {
        return Metadata.builder()
                .pagination(pagination)
                .timestamp(Instant.now())
                .build();
    }
    
    public static Metadata withTimestamp() {
        return Metadata.builder()
                .timestamp(Instant.now())
                .build();
    }
}

