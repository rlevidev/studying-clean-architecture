package com.rlevi.studying_clean_architecture.infrastructure.dto.register;

public record UserRegisterResponse(
        String message
) {
  public static UserRegisterResponse success() {
    return new UserRegisterResponse("User registered successfully.");
  }

  public static UserRegisterResponse withMessage(String message) {
    return new UserRegisterResponse(message);
  }

  public static  UserRegisterResponse fail(String message) {
    return new UserRegisterResponse(message);
  }
}
