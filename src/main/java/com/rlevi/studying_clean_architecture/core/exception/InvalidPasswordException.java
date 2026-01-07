package com.rlevi.studying_clean_architecture.core.exception;

public class InvalidPasswordException extends DomainException {
  public InvalidPasswordException(String message) {
    super(message);
  }
}
