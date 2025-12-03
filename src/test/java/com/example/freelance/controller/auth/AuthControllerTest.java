package com.example.freelance.controller.auth;

import com.example.freelance.domain.user.Role;
import com.example.freelance.dto.auth.AuthResponse;
import com.example.freelance.dto.auth.LoginRequest;
import com.example.freelance.dto.auth.RegisterRequest;
import com.example.freelance.service.auth.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_WhenValidRequest_ShouldReturnCreated() throws Exception {
        RegisterRequest request = new RegisterRequest("new@test.com", "password123", Role.FREELANCER);
        AuthResponse response = AuthResponse.builder()
                .token("jwt.token.here")
                .email("new@test.com")
                .role(Role.FREELANCER)
                .userId(1L)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt.token.here"));
    }

    @Test
    void login_WhenValidRequest_ShouldReturnOk() throws Exception {
        LoginRequest request = new LoginRequest("user@test.com", "password123");
        AuthResponse response = AuthResponse.builder()
                .token("jwt.token.here")
                .email("user@test.com")
                .role(Role.CLIENT)
                .userId(2L)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt.token.here"));
    }
}