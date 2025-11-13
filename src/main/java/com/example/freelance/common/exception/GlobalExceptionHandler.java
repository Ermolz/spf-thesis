package com.example.freelance.common.exception;

import com.example.freelance.common.dto.ApiResponse;
import com.example.freelance.common.dto.ErrorDetail;
import com.example.freelance.common.dto.ErrorResponse;
import com.example.freelance.common.dto.ValidationErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        log.warn("Validation error: {}", ex.getMessage());

        List<ErrorDetail> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> {
                    ValidationErrorDetail validationError = mapFieldError(fieldError);
                    return ErrorDetail.builder()
                            .code("VALIDATION_ERROR")
                            .message(validationError.getMessage())
                            .field(validationError.getField())
                            .rejectedValue(validationError.getRejectedValue())
                            .status(HttpStatus.BAD_REQUEST.value())
                            .path(request.getRequestURI())
                            .timestamp(Instant.now())
                            .build();
                })
                .collect(Collectors.toList());

        ApiResponse<Void> response = ApiResponse.error(errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());

        List<ErrorDetail> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> {
                    ValidationErrorDetail validationError = mapConstraintViolation(violation);
                    return ErrorDetail.builder()
                            .code("VALIDATION_ERROR")
                            .message(validationError.getMessage())
                            .field(validationError.getField())
                            .rejectedValue(validationError.getRejectedValue())
                            .status(HttpStatus.BAD_REQUEST.value())
                            .path(request.getRequestURI())
                            .timestamp(Instant.now())
                            .build();
                })
                .collect(Collectors.toList());

        ApiResponse<Void> response = ApiResponse.error(errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        log.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getMessage());

        String message = String.format("Invalid value '%s' for parameter '%s'. ", ex.getValue(), ex.getName());
        
        // Add helpful message for enum types
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) ex.getRequiredType();
            Enum<?>[] enumConstants = enumClass.getEnumConstants();
            String validValues = java.util.Arrays.stream(enumConstants)
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            message += String.format("Valid values are: %s", validValues);
        } else {
            message += String.format("Expected type: %s", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        }

        ErrorDetail error = ErrorDetail.builder()
                .code("INVALID_PARAMETER")
                .message(message)
                .field(ex.getName())
                .rejectedValue(ex.getValue())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        ApiResponse<Void> response = ApiResponse.error(error);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {
        log.warn("Business exception [{}]: {}", ex.getErrorCode(), ex.getMessage());

        ErrorDetail error = ErrorDetail.builder()
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .status(ex.getHttpStatus().value())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        ApiResponse<Void> response = ApiResponse.error(error);
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());

        String code = ex instanceof BadCredentialsException
                ? "AUTH_INVALID_CREDENTIALS"
                : "AUTH_FAILED";

        ErrorDetail error = ErrorDetail.builder()
                .code(code)
                .message("Authentication failed")
                .status(HttpStatus.UNAUTHORIZED.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        ApiResponse<Void> response = ApiResponse.error(error);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwtException(
            ExpiredJwtException ex,
            HttpServletRequest request) {
        log.warn("JWT token expired: {}", ex.getMessage());

        ErrorDetail error = ErrorDetail.builder()
                .code("AUTH_EXPIRED_TOKEN")
                .message("JWT token has expired")
                .status(HttpStatus.UNAUTHORIZED.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        ApiResponse<Void> response = ApiResponse.error(error);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ApiResponse<Void>> handleSignatureException(
            SignatureException ex,
            HttpServletRequest request) {
        log.warn("Invalid JWT signature: {}", ex.getMessage());

        ErrorDetail error = ErrorDetail.builder()
                .code("AUTH_INVALID_TOKEN")
                .message("Invalid JWT token")
                .status(HttpStatus.UNAUTHORIZED.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        ApiResponse<Void> response = ApiResponse.error(error);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());

        ErrorDetail error = ErrorDetail.builder()
                .code("ACCESS_DENIED")
                .message("Access denied")
                .status(HttpStatus.FORBIDDEN.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        ApiResponse<Void> response = ApiResponse.error(error);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unexpected error occurred", ex);

        ErrorDetail error = ErrorDetail.builder()
                .code("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        ApiResponse<Void> response = ApiResponse.error(error);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ValidationErrorDetail mapFieldError(FieldError fieldError) {
        return ValidationErrorDetail.builder()
                .field(fieldError.getField())
                .rejectedValue(fieldError.getRejectedValue())
                .message(fieldError.getDefaultMessage())
                .build();
    }

    private ValidationErrorDetail mapConstraintViolation(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath().toString();
        return ValidationErrorDetail.builder()
                .field(field)
                .rejectedValue(violation.getInvalidValue())
                .message(violation.getMessage())
                .build();
    }
}

