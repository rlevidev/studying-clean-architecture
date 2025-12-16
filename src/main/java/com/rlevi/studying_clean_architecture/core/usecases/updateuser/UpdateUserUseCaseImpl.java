package com.rlevi.studying_clean_architecture.core.usecases.updateuser;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UpdateUserUseCaseImpl implements UpdateUserUseCase {
  private final UserGateway userGateway;
  private final PasswordEncoder passwordEncoder;


  public UpdateUserUseCaseImpl(UserGateway userGateway, PasswordEncoder passwordEncoder) {
    this.userGateway = userGateway;
    this.passwordEncoder = passwordEncoder;
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

    String passwordHash = existingUser.passwordHash();
    if (user.passwordHash() != null && !user.passwordHash().isBlank()) {
      passwordHash = passwordEncoder.encode(user.passwordHash());
    }

    User updatedUser = new User(
            existingUser.id(),
            user.email(),
            user.name(),
            passwordHash,
            user.role()
    );

    return userGateway.updateUser(updatedUser);
  }
}
