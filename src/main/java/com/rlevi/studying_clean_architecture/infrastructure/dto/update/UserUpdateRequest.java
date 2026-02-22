package com.rlevi.studying_clean_architecture.infrastructure.dto.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to update a user")
public record UserUpdateRequest(
        @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters.")
        @Schema(description = "User's name", example = "Dexter Morgan")
        String name,

        @Email(message = "Invalid email format.")
        @Schema(description = "User's email", example = "dextermorgan@serialkiller.com")
        String email,

        @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters.")
        @Schema(description = "User's password", example = "password123")
        String password
) {
}
