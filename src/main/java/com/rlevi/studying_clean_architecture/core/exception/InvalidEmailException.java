package com.rlevi.studying_clean_architecture.core.exception;

public class InvalidEmailException extends DomainException {
  public InvalidEmailException(String message) {
    super(message);
  }
}
