package com.rlevi.studying_clean_architecture.infrastructure.dto.login;

public record UserLoginResponse(
        String message,
        String token,
        String refreshToken
) {
  public static UserLoginResponse success(String token, String refreshToken) {
    return new UserLoginResponse("User logged in successfully.", token, refreshToken);
  }
}
