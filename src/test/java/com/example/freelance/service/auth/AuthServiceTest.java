package com.example.freelance.service.auth;

import com.example.freelance.common.exception.ConflictException;
import com.example.freelance.common.exception.UnauthorizedException;
import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.domain.user.FreelancerProfile;
import com.example.freelance.domain.user.Role;
import com.example.freelance.domain.user.User;
import com.example.freelance.dto.auth.AuthResponse;
import com.example.freelance.dto.auth.LoginRequest;
import com.example.freelance.dto.auth.RegisterRequest;
import com.example.freelance.repository.user.ClientProfileRepository;
import com.example.freelance.repository.user.FreelancerProfileRepository;
import com.example.freelance.repository.user.UserRepository;
import com.example.freelance.security.JwtUtil;
import com.example.freelance.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FreelancerProfileRepository freelancerProfileRepository;
    @Mock
    private ClientProfileRepository clientProfileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_WhenEmailExists_ShouldThrowConflictException() {
        RegisterRequest request = new RegisterRequest("test@test.com", "password", Role.FREELANCER);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WhenFreelancer_ShouldCreateUserAndProfile() {
        RegisterRequest request = new RegisterRequest("freelancer@test.com", "pass", Role.FREELANCER);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPass");
        when(jwtUtil.generateToken(any(UserPrincipal.class), anyString())).thenReturn("token123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(request.getEmail());
        savedUser.setRole(Role.FREELANCER);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("token123");
        assertThat(response.getRole()).isEqualTo(Role.FREELANCER);

        verify(freelancerProfileRepository).save(any(FreelancerProfile.class));
        verify(clientProfileRepository, never()).save(any());
    }

    @Test
    void register_WhenClient_ShouldCreateUserAndProfile() {
        RegisterRequest request = new RegisterRequest("client@test.com", "pass", Role.CLIENT);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPass");
        when(jwtUtil.generateToken(any(UserPrincipal.class), anyString())).thenReturn("token123");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setEmail(request.getEmail());
        savedUser.setRole(Role.CLIENT);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("token123");
        assertThat(response.getRole()).isEqualTo(Role.CLIENT);

        verify(clientProfileRepository).save(any(ClientProfile.class));
        verify(freelancerProfileRepository, never()).save(any());
    }

    @Test
    void login_WhenCredentialsValid_ShouldReturnToken() {
        LoginRequest request = new LoginRequest("user@test.com", "pass");

        Authentication authentication = mock(Authentication.class);
        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(1L);
        when(authentication.getPrincipal()).thenReturn(principal);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        User user = new User();
        user.setId(1L);
        user.setEmail(request.getEmail());
        user.setRole(Role.CLIENT);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(UserPrincipal.class), anyString())).thenReturn("token123");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("token123");
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
    }

    @Test
    void login_WhenBadCredentials_ShouldThrowUnauthorizedException() {
        LoginRequest request = new LoginRequest("user@test.com", "wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }
}