package com.rlevi.studying_clean_architecture.core.usecases.loginuser;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.infrastructure.exception.AuthenticationException;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;
import com.rlevi.studying_clean_architecture.core.gateway.PasswordEncoderGateway;

public class LoginUserUseCaseImpl implements LoginUserUseCase {
  private final UserGateway userGateway;

  private final PasswordEncoderGateway passwordEncoderGateway;

  public LoginUserUseCaseImpl(UserGateway userGateway, PasswordEncoderGateway passwordEncoderGateway) {
    this.userGateway = userGateway;
    this.passwordEncoderGateway = passwordEncoderGateway;
  }

  @Override
  public User execute(User user) {
    if (user == null || user.email() == null || user.passwordHash() == null) {
      throw new AuthenticationException("Invalid email or password. Please try again.");
    }

    User foundUser = userGateway.findUserByEmail(user.email())
      .orElseThrow(() -> new AuthenticationException("Invalid email or password. Please try again."));

    if (!passwordEncoderGateway.matches(user.passwordHash(), foundUser.passwordHash())) {
      throw new AuthenticationException("Invalid email or password. Please try again.");
    }

    return foundUser;
  }
}
