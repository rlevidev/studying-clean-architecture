package com.rlevi.studying_clean_architecture.core.usecases.finduserbyid;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;

import java.util.Optional;

public class FindUserByIdUseCaseImpl implements FindUserByIdUseCase {
  private final UserGateway userGateway;

  public FindUserByIdUseCaseImpl(UserGateway userGateway) {
    this.userGateway = userGateway;
  }

  @Override
  public Optional<User> execute(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("User ID cannot be null.");
    }

    return userGateway.findUserById(id);
  }
}
