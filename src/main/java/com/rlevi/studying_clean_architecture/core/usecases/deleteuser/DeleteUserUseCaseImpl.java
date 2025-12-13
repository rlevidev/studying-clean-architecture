package com.rlevi.studying_clean_architecture.core.usecases.deleteuser;

import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;

public class DeleteUserUseCaseImpl implements DeleteUserUseCase {
  private final UserGateway userGateway;

  public DeleteUserUseCaseImpl(UserGateway userGateway) {
    this.userGateway = userGateway;
  }

  @Override
  public void execute(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("Id cannot be null.");
    } else if (userGateway.findUserById(id).isEmpty()) {
      throw new IllegalArgumentException("User not found with ID: " + id);
    }

    userGateway.deleteUser(id);
  }
}
