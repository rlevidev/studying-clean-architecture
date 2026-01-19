package com.rlevi.studying_clean_architecture.infrastructure.dto.refreshtoken;

public record RefreshTokenResponse(
        String message,
        String accessToken,
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
