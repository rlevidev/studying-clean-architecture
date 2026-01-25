package com.rlevi.studying_clean_architecture.core.exception;

public class InvalidRefreshTokenException extends DomainException {
  public InvalidRefreshTokenException(String message) {
    super(message);
  }
}
