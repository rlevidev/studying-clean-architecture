package com.rlevi.studying_clean_architecture.core.usecases.createuser;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.enums.Role;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CreateUserUseCaseImpl implements CreateUserUseCase {
  private final UserGateway userGateway;
  private final PasswordEncoder passwordEncoder;

  public CreateUserUseCaseImpl(UserGateway userGateway, PasswordEncoder passwordEncoder) {
    this.userGateway = userGateway;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public User execute(User user) {
    if (userGateway.verifyExistsByEmail(user.email())) {
      throw new IllegalArgumentException("Email já está cadastrado.");
    }

    String encryptedPassword = passwordEncoder.encode(user.passwordHash());
    
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
