package com.rlevi.studying_clean_architecture.infrastructure.dto.response;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
    public UserResponse {
        // Validation of required fields
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
    }
}
