package com.rlevi.studying_clean_architecture.infrastructure.dto.login;

public record UserLoginResponse(
        String message
) {
  public static UserLoginResponse success() {
    return new UserLoginResponse("User logged in successfully.");
  }

  public static UserLoginResponse withMessage(String message) {
    return new UserLoginResponse(message);
  }

  public static UserLoginResponse fail(String message) {
    return new UserLoginResponse(message);
  }
}
