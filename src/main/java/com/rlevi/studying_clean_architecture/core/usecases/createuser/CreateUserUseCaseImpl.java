package com.rlevi.studying_clean_architecture.core.usecases.createuser;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.enums.Role;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;

public class CreateUserUseCaseImpl implements CreateUserUseCase {
  private final UserGateway userGateway;

  public CreateUserUseCaseImpl(UserGateway userGateway) {
    this.userGateway = userGateway;
  }

  @Override
  public User execute(User user) {
    if (userGateway.verifyExistsByEmail(user.email())) {
      throw new IllegalArgumentException("Email já está cadastrado.");
    }

    var userToSave = new User(
            null,
            user.email(),
            user.name(),
            user.passwordHash(),
            user.role()
    );

    return userGateway.createUser(userToSave);
  }
}
