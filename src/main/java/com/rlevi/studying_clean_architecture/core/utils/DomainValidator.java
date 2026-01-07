package com.rlevi.studying_clean_architecture.core.utils;

import com.rlevi.studying_clean_architecture.core.exception.InvalidEmailException;

public final class DomainValidator {
  private DomainValidator() {}

  public static void validateEmail(String email) {
    if (email == null || !email.contains("@")) {
      throw new InvalidEmailException("Invalid email format");
    }
  }

  public static void validateName(String name) {
    if (name == null || name.length() < 3) {
      throw new IllegalArgumentException("Name too short");
    }
  }
}
