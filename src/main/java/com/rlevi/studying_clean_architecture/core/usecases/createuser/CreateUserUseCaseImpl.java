package com.rlevi.studying_clean_architecture.core.usecases.createuser;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.infrastructure.exception.DuplicateResourceException;
import com.rlevi.studying_clean_architecture.core.enums.Role;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;
import com.rlevi.studying_clean_architecture.core.gateway.PasswordEncoderGateway;

public class CreateUserUseCaseImpl implements CreateUserUseCase {
  private final UserGateway userGateway;
  private final PasswordEncoderGateway passwordEncoderGateway;

  public CreateUserUseCaseImpl(UserGateway userGateway, PasswordEncoderGateway passwordEncoderGateway) {
    this.userGateway = userGateway;
    this.passwordEncoderGateway = passwordEncoderGateway;
  }

  @Override
  public User execute(User user) {
    if (userGateway.verifyExistsByEmail(user.email())) {
      throw new DuplicateResourceException("The email provided is already in use. Please use another email or log in.");
    }

    String encryptedPassword = passwordEncoderGateway.encode(user.passwordHash());
    
    var userToSave = new User(
            null,
            user.email(),
            user.name(),
            encryptedPassword,
            Role.USER,
            null,
            null
    );

    return userGateway.createUser(userToSave);
  }
}
