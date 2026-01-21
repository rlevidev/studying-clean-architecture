package com.rlevi.studying_clean_architecture.core.usecases.createuser;

import com.rlevi.studying_clean_architecture.core.entities.AuthResult;
import com.rlevi.studying_clean_architecture.core.entities.RefreshToken;
import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.exception.UserAlreadyExistsException;
import com.rlevi.studying_clean_architecture.core.gateway.RefreshTokenGateway;
import com.rlevi.studying_clean_architecture.core.gateway.TokenGateway;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;
import com.rlevi.studying_clean_architecture.core.gateway.PasswordEncoderGateway;
import com.rlevi.studying_clean_architecture.core.utils.DomainValidator;

import java.time.Instant;

public class CreateUserUseCaseImpl implements CreateUserUseCase {
  private final UserGateway userGateway;
  private final PasswordEncoderGateway passwordEncoderGateway;
  private final TokenGateway tokenGateway;
  private final RefreshTokenGateway refreshTokenGateway;

  public CreateUserUseCaseImpl(UserGateway userGateway, PasswordEncoderGateway passwordEncoderGateway, TokenGateway tokenGateway, RefreshTokenGateway refreshTokenGateway) {
    this.userGateway = userGateway;
    this.passwordEncoderGateway = passwordEncoderGateway;
    this.tokenGateway = tokenGateway;
    this.refreshTokenGateway = refreshTokenGateway;
  }

  @Override
  public AuthResult execute(User user) {
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

    User createdUser = userGateway.createUser(userToSave);

    // Generate tokens
    String accessToken = tokenGateway.generateAccessToken(createdUser.email());
    String refreshTokenValue = tokenGateway.generateRefreshToken(createdUser.email());
    Instant expiryDate = tokenGateway.extractExpiration(refreshTokenValue);

    // Save Refresh Token
    RefreshToken refreshToken = new RefreshToken(
            null,
            refreshTokenValue,
            createdUser.id(),
            expiryDate,
            Instant.now(),
            false,
            null
    );
    refreshTokenGateway.save(refreshToken);

    return new AuthResult(createdUser, accessToken, refreshTokenValue);
  }
}
