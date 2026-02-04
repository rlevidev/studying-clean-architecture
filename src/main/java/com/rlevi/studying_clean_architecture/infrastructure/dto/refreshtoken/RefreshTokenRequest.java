package com.rlevi.studying_clean_architecture.infrastructure.dto.refreshtoken;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to obtain a new access token using a refresh token")
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required.")
        @Schema(description = "Refresh token used to obtain a new access token", example = "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6")
        String refreshToken
) {
}
