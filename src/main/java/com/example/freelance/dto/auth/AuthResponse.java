package com.example.freelance.dto.auth;

import com.example.freelance.domain.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing authentication token and user information")
public class AuthResponse {
    @Schema(description = "JWT authentication token (use in Authorization header as 'Bearer <token>')", 
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    private String token;
    
    @Schema(description = "User email address", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "User role", example = "FREELANCER", allowableValues = {"FREELANCER", "CLIENT", "ADMIN"})
    private Role role;
    
    @Schema(description = "User unique identifier", example = "1")
    private Long userId;
}

