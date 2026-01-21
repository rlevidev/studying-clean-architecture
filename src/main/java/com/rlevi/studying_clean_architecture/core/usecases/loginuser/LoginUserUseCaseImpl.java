package com.rlevi.studying_clean_architecture.core.usecases.loginuser;

import com.rlevi.studying_clean_architecture.core.entities.AuthResult;
import com.rlevi.studying_clean_architecture.core.entities.RefreshToken;
import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.gateway.RefreshTokenGateway;
import com.rlevi.studying_clean_architecture.core.gateway.TokenGateway;
import com.rlevi.studying_clean_architecture.infrastructure.exception.AuthenticationException;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;
import com.rlevi.studying_clean_architecture.core.gateway.PasswordEncoderGateway;

import java.time.Instant;

public class LoginUserUseCaseImpl implements LoginUserUseCase {
  private final UserGateway userGateway;
  private final PasswordEncoderGateway passwordEncoderGateway;
  private final RefreshTokenGateway refreshTokenGateway;
  private final TokenGateway tokenGateway;

  public LoginUserUseCaseImpl(UserGateway userGateway, PasswordEncoderGateway passwordEncoderGateway, RefreshTokenGateway refreshTokenGateway, TokenGateway tokenGateway) {
    this.userGateway = userGateway;
    this.passwordEncoderGateway = passwordEncoderGateway;
    this.refreshTokenGateway = refreshTokenGateway;
    this.tokenGateway = tokenGateway;
  }

  @Override
  public AuthResult execute(User user) {
    if (user == null || user.email() == null || user.passwordHash() == null || user.passwordHash().isBlank()) {
      throw new AuthenticationException("Invalid email or password. Please try again.");
    }

    User foundUser = userGateway.findUserByEmail(user.email())
      .orElseThrow(() -> new AuthenticationException("Invalid email or password. Please try again."));

    if (!passwordEncoderGateway.matches(user.passwordHash(), foundUser.passwordHash())) {
      throw new AuthenticationException("Invalid email or password. Please try again.");
    }

    // Generate tokens
    String accessToken = tokenGateway.generateAccessToken(foundUser.email());
    String refreshTokenValue = tokenGateway.generateRefreshToken(foundUser.email());
    Instant expiryDate = tokenGateway.extractExpiration(refreshTokenValue);

    // Save Refresh Token
    RefreshToken refreshToken = new RefreshToken(
            null,
            refreshTokenValue,
            foundUser.id(),
            expiryDate,
            Instant.now(),
            false,
            null
    );
    refreshTokenGateway.save(refreshToken);

    return new AuthResult(foundUser, accessToken, refreshTokenValue);
  }
}
