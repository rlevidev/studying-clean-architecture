package com.rlevi.studying_clean_architecture.core.usecases.updateuser;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.exception.UserNotFoundException;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;
import com.rlevi.studying_clean_architecture.core.gateway.PasswordEncoderGateway;

public class UpdateUserUseCaseImpl implements UpdateUserUseCase {
  private final UserGateway userGateway;
  private final PasswordEncoderGateway passwordEncoderGateway;


  public UpdateUserUseCaseImpl(UserGateway userGateway, PasswordEncoderGateway passwordEncoderGateway) {
    this.userGateway = userGateway;
    this.passwordEncoderGateway = passwordEncoderGateway;
  }

  @Override
  public User execute(User user) {
    if (user == null) {
      throw new UserNotFoundException("User cannot be null.");
    } else if (user.id() == null) {
      throw new UserNotFoundException("User ID is required for update");
    }

    var existingUser = userGateway.findUserById(user.id())
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + user.id()));

    String name = existingUser.name();
    if (user.name() != null && !user.name().isBlank()) {
      name = user.name();
    }

    String email = existingUser.email();
    if (user.email() != null && !user.email().isBlank()) {
      email = user.email();
    }

    String passwordHash = existingUser.passwordHash();
    if (user.passwordHash() != null && !user.passwordHash().isBlank()) {
      passwordHash = passwordEncoderGateway.encode(user.passwordHash());
    }

    User updatedUser = new User(
            existingUser.id(),
            email,
            name,
            passwordHash,
            existingUser.createdAt(),
            existingUser.updatedAt()
    );

    return userGateway.updateUser(updatedUser);
  }
}
