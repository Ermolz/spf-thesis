package com.example.freelance.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pagination information")
public class PaginationInfo {
    
    @Schema(description = "Current page number (0-indexed)", example = "0")
    private Integer page;
    
    @Schema(description = "Page size", example = "20")
    private Integer size;
    
    @Schema(description = "Total number of pages", example = "10")
    private Integer totalPages;
    
    @Schema(description = "Total number of elements", example = "195")
    private Long totalElements;
    
    @Schema(description = "Whether this is the first page", example = "true")
    private Boolean first;
    
    @Schema(description = "Whether this is the last page", example = "false")
    private Boolean last;
    
    @Schema(description = "Number of elements in current page", example = "20")
    private Integer numberOfElements;
    
    public static PaginationInfo fromPage(org.springframework.data.domain.Page<?> page) {
        return PaginationInfo.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .build();
    }
}

