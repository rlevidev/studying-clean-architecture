package com.rlevi.studying_clean_architecture.infrastructure.dto.register;

public record UserRegisterResponse(
        String message,
        String token
) {
  public static UserRegisterResponse success(String token) {
    return new UserRegisterResponse("User registered successfully.", token);
  }
}
