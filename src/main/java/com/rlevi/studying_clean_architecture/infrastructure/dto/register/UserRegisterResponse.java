package com.rlevi.studying_clean_architecture.infrastructure.dto.register;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserRegisterResponse(
        @Schema(description = "Success message", example = "User registered successfully.")
        String message,

        @Schema(description = "JWT Access Token used for authenticating requests", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Refresh token used to obtain a new access token", example = "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6")
        String refreshToken
) {
  public static UserRegisterResponse success(String token, String refreshToken) {
    return new UserRegisterResponse("User registered successfully.", token, refreshToken);
  }
}
