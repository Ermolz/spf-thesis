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
@Schema(description = "Error detail information")
public class ErrorDetail {
    
    @Schema(description = "Error code", example = "VALIDATION_ERROR")
    private String code;
    
    @Schema(description = "Error message", example = "Validation failed")
    private String message;
    
    @Schema(description = "Field name (for validation errors)", example = "email")
    private String field;
    
    @Schema(description = "Rejected value (for validation errors)", example = "invalid-email")
    private Object rejectedValue;
    
    @Schema(description = "HTTP status code", example = "400")
    private Integer status;
    
    @Schema(description = "Request path", example = "/api/projects")
    private String path;
    
    @Schema(description = "Timestamp when error occurred", example = "2024-01-15T10:30:00Z")
    private java.time.Instant timestamp;
    
    public static ErrorDetail fromValidationError(com.example.freelance.common.dto.ValidationErrorDetail validationError) {
        return ErrorDetail.builder()
                .code("VALIDATION_ERROR")
                .message(validationError.getMessage())
                .field(validationError.getField())
                .rejectedValue(validationError.getRejectedValue())
                .build();
    }
}

