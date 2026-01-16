package com.rlevi.studying_clean_architecture.core.usecases.createuser;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.exception.UserAlreadyExistsException;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;
import com.rlevi.studying_clean_architecture.core.gateway.PasswordEncoderGateway;
import com.rlevi.studying_clean_architecture.core.utils.DomainValidator;

public class CreateUserUseCaseImpl implements CreateUserUseCase {
  private final UserGateway userGateway;
  private final PasswordEncoderGateway passwordEncoderGateway;

  public CreateUserUseCaseImpl(UserGateway userGateway, PasswordEncoderGateway passwordEncoderGateway) {
    this.userGateway = userGateway;
    this.passwordEncoderGateway = passwordEncoderGateway;
  }

  @Override
  public User execute(User user) {
    DomainValidator.validateEmail(user.email());
    DomainValidator.validateName(user.name());

    if (userGateway.verifyExistsByEmail(user.email())) {
      throw new UserAlreadyExistsException("The email provided is already in use. Please use another email or log in.");
    }

    String encryptedPassword = passwordEncoderGateway.encode(user.passwordHash());
    
    var userToSave = new User(
            null,
            user.email(),
            user.name(),
            encryptedPassword,
            null,
            null
    );

    return userGateway.createUser(userToSave);
  }
}
