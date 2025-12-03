package com.example.freelance.dto.user;

import com.example.freelance.domain.user.Role;
import com.example.freelance.domain.user.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private Role role;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}

