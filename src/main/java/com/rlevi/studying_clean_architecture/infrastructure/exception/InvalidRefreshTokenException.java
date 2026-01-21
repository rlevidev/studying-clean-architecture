package com.rlevi.studying_clean_architecture.infrastructure.exception;

public class InvalidRefreshTokenException extends RuntimeException {
  public InvalidRefreshTokenException(String message) {
    super(message);
  }

  public InvalidRefreshTokenException(String message, Throwable cause) {
    super(message, cause);
  }
}
