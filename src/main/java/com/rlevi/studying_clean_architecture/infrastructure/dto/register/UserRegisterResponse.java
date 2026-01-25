package com.rlevi.studying_clean_architecture.infrastructure.dto.register;

public record UserRegisterResponse(
        String message,
        String token,
        String refreshToken
) {
  public static UserRegisterResponse success(String token, String refreshToken) {
    return new UserRegisterResponse("User registered successfully.", token, refreshToken);
  }
}
