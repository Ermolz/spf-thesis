package com.example.freelance.service.auth;

import com.example.freelance.common.exception.ConflictException;
import com.example.freelance.common.exception.UnauthorizedException;
import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.domain.user.FreelancerProfile;
import com.example.freelance.domain.user.Role;
import com.example.freelance.domain.user.User;
import com.example.freelance.domain.user.UserStatus;
import com.example.freelance.dto.auth.AuthResponse;
import com.example.freelance.dto.auth.LoginRequest;
import com.example.freelance.dto.auth.RegisterRequest;
import com.example.freelance.repository.user.ClientProfileRepository;
import com.example.freelance.repository.user.FreelancerProfileRepository;
import com.example.freelance.repository.user.UserRepository;
import com.example.freelance.security.JwtUtil;
import com.example.freelance.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists", "EMAIL_ALREADY_EXISTS");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);

        user = userRepository.save(user);

        if (request.getRole() == Role.FREELANCER) {
            FreelancerProfile profile = new FreelancerProfile();
            profile.setUser(user);
            profile.setDisplayName(request.getEmail().split("@")[0]);
            freelancerProfileRepository.save(profile);
            user.setFreelancerProfile(profile);
        } else if (request.getRole() == Role.CLIENT) {
            ClientProfile profile = new ClientProfile();
            profile.setUser(user);
            clientProfileRepository.save(profile);
            user.setClientProfile(profile);
        }

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String token = jwtUtil.generateToken(userPrincipal, user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .userId(user.getId())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            String token = jwtUtil.generateToken(userPrincipal, user.getRole().name());

            return AuthResponse.builder()
                    .token(token)
                    .email(user.getEmail())
                    .role(user.getRole())
                    .userId(user.getId())
                    .build();
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password", "AUTH_INVALID_CREDENTIALS");
        }
    }
}

