package com.example.freelance.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/api/**")
                .packagesToScan("com.example.freelance.controller")
                .build();
    }

    @Bean
    public OpenAPI freelanceMarketplaceOpenAPI() {
        String description = """
                Comprehensive REST API for a freelance marketplace platform.
                
                ## Features
                - **User Management**: Registration, authentication, and profile management for Freelancers and Clients
                - **Project Management**: Create, search, and manage projects with categories and tags
                - **Proposal System**: Freelancers can submit proposals for projects
                - **Assignment Management**: Handle contracts between clients and freelancers
                - **Task Management**: Break down assignments into trackable tasks with file attachments
                - **Messaging**: Real-time chat between clients and freelancers
                - **Payment Processing**: Escrow system for secure payments and payouts
                - **Review System**: Rate and review completed work
                
                ## Authentication
                All endpoints except `/api/auth/**` require JWT authentication.
                Include the JWT token in the `Authorization` header: `Bearer <token>`
                
                ## Error Handling
                The API uses standardized error responses with error codes:
                - **400 Bad Request**: Invalid input data or business rule violation
                - **401 Unauthorized**: Missing or invalid authentication token
                - **403 Forbidden**: Insufficient permissions for the requested action
                - **404 Not Found**: Requested resource does not exist
                - **409 Conflict**: Resource conflict (e.g., duplicate proposal)
                - **500 Internal Server Error**: Unexpected server error
                
                ## Rate Limiting
                API rate limits may apply. Check response headers for rate limit information.
                """;
        
        return new OpenAPI()
                .info(new Info()
                        .title("Freelance Marketplace API")
                        .description(description)
                        .version("1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.freelance-marketplace.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from /api/auth/login endpoint")));
    }
}

