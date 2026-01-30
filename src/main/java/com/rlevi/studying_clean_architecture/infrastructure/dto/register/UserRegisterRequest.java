package com.rlevi.studying_clean_architecture.infrastructure.dto.register;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
        @NotBlank(message = "Name is required.")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters.")
        @Schema(description = "User's name", example = "Dexter Morgan")
        String name,

        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email format.")
        @Schema(description = "User's email", example = "dextermorgan@serialkiller.com")
        String email,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, message = "Password must have at least 8 characters.")
        @Schema(description = "User's password", example = "password123")
        String password
) {
}
