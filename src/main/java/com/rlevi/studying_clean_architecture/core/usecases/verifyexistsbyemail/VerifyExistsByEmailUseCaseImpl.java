package com.rlevi.studying_clean_architecture.core.usecases.verifyexistsbyemail;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;

import java.util.Optional;

public class VerifyExistsByEmailUseCaseImpl implements VerifyExistsByEmailUseCase {
  private final UserGateway userGateway;

  public VerifyExistsByEmailUseCaseImpl(UserGateway userGateway) {
    this.userGateway = userGateway;
  }

  @Override
  public boolean execute(String email) {
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty.");
    }

    return userGateway.verifyExistsByEmail(email);
  }
}
