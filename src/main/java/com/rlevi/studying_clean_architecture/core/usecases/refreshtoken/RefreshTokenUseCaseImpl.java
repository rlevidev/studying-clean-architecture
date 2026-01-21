package com.rlevi.studying_clean_architecture.core.usecases.refreshtoken;

import com.rlevi.studying_clean_architecture.core.entities.AuthResult;
import com.rlevi.studying_clean_architecture.core.entities.RefreshToken;
import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.exception.DomainException;
import com.rlevi.studying_clean_architecture.core.gateway.RefreshTokenGateway;
import com.rlevi.studying_clean_architecture.core.gateway.TokenGateway;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;
import com.rlevi.studying_clean_architecture.infrastructure.exception.InvalidRefreshTokenException;

import java.time.Instant;

public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {
  private final UserGateway userGateway;
  private final TokenGateway tokenGateway;
  private final RefreshTokenGateway refreshTokenGateway;

  public RefreshTokenUseCaseImpl(UserGateway userGateway, TokenGateway tokenGateway, RefreshTokenGateway refreshTokenGateway) {
    this.userGateway = userGateway;
    this.tokenGateway = tokenGateway;
    this.refreshTokenGateway = refreshTokenGateway;
  }

  @Override
  public AuthResult execute(String refreshToken) {
    // 1. Find refresh token in the database (only if not revoked)
    RefreshToken currentToken = refreshTokenGateway.findByTokenAndRevokedFalse(refreshToken)
            .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found or already revoked."));

    // 2. Check if the physical token (JWT) is expired
    if (currentToken.expiryDate().isBefore(Instant.now())) {
      refreshTokenGateway.revokeByToken(refreshToken, null);
      throw new InvalidRefreshTokenException("Refresh token has expired. Please login again.");
    }

    // 3. Validate token integrity (optional, as it's already in DB, but good for security)
    // Actually, TokenGateway should probably have a validation method if we want to be strict.
    // But for now we use extractUsername which will fail if token is invalid.
    String email = tokenGateway.extractUsername(refreshToken);

    // 4. Find the associated user
    User user = userGateway.findUserByEmail(email)
            .orElseThrow(() -> new DomainException("User associated with token not found."));

    // 5. Validate if the token belongs to the found user
    if (!currentToken.userId().equals(user.id())) {
      throw new InvalidRefreshTokenException("Token mismatch: Refresh token does not belong to this user.");
    }

    // 6. ROTATION: Generate new Refresh Token and Access Token
    String newAccessToken = tokenGateway.generateAccessToken(user.email());
    String newRefreshTokenValue = tokenGateway.generateRefreshToken(user.email());
    Instant newExpiryDate = tokenGateway.extractExpiration(newRefreshTokenValue);

    RefreshToken newRefreshToken = new RefreshToken(
            null,
            newRefreshTokenValue,
            user.id(),
            newExpiryDate,
            Instant.now(),
            false,
            null
    );

    // 7. Revoke the old token and link to the new one (for traceability)
    refreshTokenGateway.revokeByToken(refreshToken, newRefreshTokenValue);

    // 8. Save the new token
    RefreshToken savedRefreshToken = refreshTokenGateway.save(newRefreshToken);

    return new AuthResult(user, newAccessToken, savedRefreshToken.token());
  }
}
