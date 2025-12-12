package com.rlevi.studying_clean_architecture.core.usecases.updateuser;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;

public class UpdateUserUseCaseImpl implements UpdateUserUseCase {
  private final UserGateway userGateway;

  public UpdateUserUseCaseImpl(UserGateway userGateway) {
    this.userGateway = userGateway;
  }

  @Override
  public User execute(User user) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null.");
    } else if (user.id() == null) {
      throw new IllegalArgumentException("User ID is required for update");
    }

    var existingUser = userGateway.findUserById(user.id())
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + user.id()));

    return userGateway.updateUser(user);
  }
}
