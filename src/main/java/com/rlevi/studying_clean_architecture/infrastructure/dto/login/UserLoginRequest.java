package com.rlevi.studying_clean_architecture.infrastructure.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserLoginRequest(
        @Schema(description = "User's email", example = "dextermorgan@serialkiller.com")
        String email,

        @Schema(description = "User's password", example = "password123")
        String password
) {
}
