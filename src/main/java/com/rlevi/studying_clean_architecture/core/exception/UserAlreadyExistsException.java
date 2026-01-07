package com.rlevi.studying_clean_architecture.core.exception;

public class UserAlreadyExistsException extends DomainException {
  public UserAlreadyExistsException(String message) {
    super(message);
  }
}
