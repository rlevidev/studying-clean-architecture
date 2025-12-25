package com.rlevi.studying_clean_architecture.infrastructure.dto.login;

public record UserLoginResponse(
        String message,
        String token
) {
  public static UserLoginResponse success(String token) {
    return new UserLoginResponse("User logged in successfully.", token);
  }

  public static UserLoginResponse withMessage(String message) {
    return new UserLoginResponse(message, null);
  }

  public static UserLoginResponse fail(String message) {
    return new UserLoginResponse(message, null);
  }
}
