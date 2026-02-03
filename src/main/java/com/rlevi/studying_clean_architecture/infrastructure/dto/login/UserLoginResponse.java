package com.rlevi.studying_clean_architecture.infrastructure.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserLoginResponse(
        @Schema(description = "Success message", example = "User logged in successfully.")
        String message,

        @Schema(description = "JWT Access Token used for authenticating requests", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Refresh token used to obtain a new access token", example = "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6")
        String refreshToken
) {
  public static UserLoginResponse success(String token, String refreshToken) {
    return new UserLoginResponse("User logged in successfully.", token, refreshToken);
  }
}
