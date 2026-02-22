package com.rlevi.studying_clean_architecture.infrastructure.dto.refreshtoken;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing refreshed tokens")
public record RefreshTokenResponse(
        @Schema(description = "Success message", example = "Token refreshed successfully.")
        String message,

        @Schema(description = "JWT Access Token used for authenticating requests", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Refresh token used to obtain a new access token", example = "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6")
        String refreshToken
) {
  public static RefreshTokenResponse success(String accessToken, String refreshToken) {
    return new RefreshTokenResponse(
            "Token refreshed successfully.",
            accessToken,
            refreshToken
    );
  }
}
