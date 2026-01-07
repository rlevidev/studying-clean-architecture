package com.rlevi.studying_clean_architecture.core.exception;

public class UserNotFoundException extends DomainException {
  public UserNotFoundException(String message) {
    super(message);
  }
}
