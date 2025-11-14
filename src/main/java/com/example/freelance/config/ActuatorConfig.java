package com.example.freelance.config;

import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for Spring Boot Actuator.
 * Provides custom information endpoints and health checks.
 */
@Configuration
public class ActuatorConfig {

    /**
     * Custom info contributor that adds application-specific information.
     */
    @Bean
    public InfoContributor customInfoContributor() {
        return builder -> {
            Map<String, Object> details = new HashMap<>();
            details.put("name", "Freelance Marketplace API");
            details.put("description", "REST API for a freelance marketplace platform");
            details.put("version", "1.0.0");
            details.put("buildTime", Instant.now().toString());
            details.put("environment", System.getProperty("spring.profiles.active", "default"));
            
            Map<String, Object> features = new HashMap<>();
            features.put("authentication", "JWT-based authentication");
            features.put("fileUpload", "Task attachments with validation");
            features.put("payments", "Escrow payment system");
            features.put("messaging", "Real-time chat between clients and freelancers");
            features.put("reviews", "Rating and review system");
            details.put("features", features);
            
            builder.withDetails(details);
        };
    }
}

