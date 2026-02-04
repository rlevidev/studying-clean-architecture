package com.rlevi.studying_clean_architecture.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Response to check if a user exists")
public record UserResponse(
        @Schema(description = "User's ID", example = "1")
        Long id,

        @Schema(description = "User's email", example = "dextermorgan@serialkiller.com")
        String email,

        @Schema(description = "User's name", example = "Dexter Morgan")
        String name,

        @Schema(description = "User's creation date", example = "2026-01-01T00:00:00Z")
        Instant createdAt,

        @Schema(description = "User's update date", example = "2026-01-01T00:00:00Z")
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
