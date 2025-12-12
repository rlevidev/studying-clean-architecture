package com.rlevi.studying_clean_architecture.core.usecases.finduserbyemail;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;

import java.util.Optional;

public class FindUserByEmailUseCaseImpl implements FindUserByEmailUseCase {
  private final UserGateway userGateway;

  public FindUserByEmailUseCaseImpl(UserGateway userGateway) {
    this.userGateway = userGateway;
  }

  @Override
  public Optional<User> execute(String email) {
    if (email == null) {
      throw new IllegalArgumentException("Email cannot be null.");
    }

    return userGateway.findUserByEmail(email);
  }
}
