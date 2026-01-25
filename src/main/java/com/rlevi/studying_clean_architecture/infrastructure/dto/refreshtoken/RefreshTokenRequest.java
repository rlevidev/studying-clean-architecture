package com.rlevi.studying_clean_architecture.infrastructure.dto.refreshtoken;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required.")
        String refreshToken
) {
}
