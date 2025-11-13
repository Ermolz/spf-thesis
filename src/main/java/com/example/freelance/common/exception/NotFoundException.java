package com.example.freelance.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {
    private final String entityType;
    private final String identifier;

    public NotFoundException(String entityType, String identifier) {
        super(
                String.format("%s with identifier '%s' not found", entityType, identifier),
                entityType != null ? entityType.toUpperCase() + "_NOT_FOUND" : "RESOURCE_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
        this.entityType = entityType;
        this.identifier = identifier;
    }

    public NotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
        this.entityType = null;
        this.identifier = null;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getIdentifier() {
        return identifier;
    }
}

