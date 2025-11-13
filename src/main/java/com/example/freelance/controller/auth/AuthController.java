package com.example.freelance.controller.auth;

import com.example.freelance.common.dto.ApiResponse;
import com.example.freelance.common.util.ResponseUtil;
import com.example.freelance.dto.auth.AuthResponse;
import com.example.freelance.dto.auth.LoginRequest;
import com.example.freelance.dto.auth.RegisterRequest;
import com.example.freelance.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and authentication endpoints. No authentication required.")
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Register a new user",
            description = """
                    Creates a new user account in the system. The user can register as either a FREELANCER or CLIENT.
                    Upon successful registration, a JWT token is returned that should be used for subsequent authenticated requests.
                    
                    **Registration Process:**
                    1. Validates email format and password strength
                    2. Checks if email is already registered
                    3. Creates user account with the specified role
                    4. Generates and returns JWT token
                    
                    **Password Requirements:**
                    - Minimum 6 characters
                    - Should contain a mix of letters and numbers for better security
                    
                    **Role Options:**
                    - `FREELANCER`: For freelancers who want to offer services
                    - `CLIENT`: For clients who want to post projects
                    - `ADMIN`: Reserved for system administrators (typically created separately)
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    name = "Freelancer Registration",
                                    value = """
                                            {
                                              "email": "john.doe@example.com",
                                              "password": "securePass123",
                                              "role": "FREELANCER"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User successfully registered",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "email": "john.doe@example.com",
                                                "role": "FREELANCER",
                                                "userId": 1
                                              },
                                              "metadata": {
                                                "timestamp": "2024-01-15T10:30:00Z"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid input data",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Validation Error",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "errors": [
                                                        {
                                                          "code": "VALIDATION_ERROR",
                                                          "message": "Email must be valid",
                                                          "field": "email",
                                                          "rejectedValue": "invalid-email",
                                                          "status": 400,
                                                          "path": "/api/auth/register",
                                                          "timestamp": "2024-01-15T10:30:00Z"
                                                        },
                                                        {
                                                          "code": "VALIDATION_ERROR",
                                                          "message": "Password must be at least 6 characters",
                                                          "field": "password",
                                                          "rejectedValue": "123",
                                                          "status": 400,
                                                          "path": "/api/auth/register",
                                                          "timestamp": "2024-01-15T10:30:00Z"
                                                        }
                                                      ]
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Email Already Exists",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "errors": [
                                                        {
                                                          "code": "EMAIL_ALREADY_EXISTS",
                                                          "message": "Email already registered",
                                                          "status": 400,
                                                          "path": "/api/auth/register",
                                                          "timestamp": "2024-01-15T10:30:00Z"
                                                        }
                                                      ]
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtil.successWithTimestamp(response));
    }

    @Operation(
            summary = "Authenticate user and get JWT token",
            description = """
                    Authenticates an existing user with email and password, returning a JWT token for authenticated requests.
                    
                    **Authentication Process:**
                    1. Validates email and password format
                    2. Verifies credentials against stored user data
                    3. Generates JWT token with user information
                    4. Returns token with user details
                    
                    **Token Usage:**
                    - Include token in `Authorization` header: `Bearer <token>`
                    - Token expires after 24 hours (configurable)
                    - Use `/api/auth/login` again to get a new token
                    
                    **Security Notes:**
                    - Passwords are never returned in responses
                    - Tokens should be stored securely on client side
                    - Do not expose tokens in client-side code or logs
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User login credentials",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "Login Example",
                                    value = """
                                            {
                                              "email": "john.doe@example.com",
                                              "password": "securePass123"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "email": "john.doe@example.com",
                                                "role": "FREELANCER",
                                                "userId": 1
                                              },
                                              "metadata": {
                                                "timestamp": "2024-01-15T10:30:00Z"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid input data",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                                            {
                                              "success": false,
                                              "errors": [
                                                {
                                                  "code": "VALIDATION_ERROR",
                                                  "message": "Email must be valid",
                                                  "field": "email",
                                                  "rejectedValue": "invalid-email",
                                                  "status": 400,
                                                  "path": "/api/auth/login",
                                                  "timestamp": "2024-01-15T10:30:00Z"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid credentials",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Invalid Credentials",
                                    value = """
                                            {
                                              "success": false,
                                              "errors": [
                                                {
                                                  "code": "AUTH_INVALID_CREDENTIALS",
                                                  "message": "Authentication failed",
                                                  "status": 401,
                                                  "path": "/api/auth/login",
                                                  "timestamp": "2024-01-15T10:30:00Z"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ResponseUtil.successWithTimestamp(response));
    }
}

