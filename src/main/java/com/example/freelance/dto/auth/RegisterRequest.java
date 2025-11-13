package com.example.freelance.dto.auth;

import com.example.freelance.domain.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for user registration")
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "User email address", example = "john.doe@example.com", required = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "User password (minimum 6 characters)", example = "securePass123", required = true, minLength = 6)
    private String password;

    @NotNull(message = "Role is required")
    @Schema(description = "User role in the system", example = "FREELANCER", allowableValues = {"FREELANCER", "CLIENT", "ADMIN"}, required = true)
    private Role role;
}

