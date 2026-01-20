package com.rlevi.studying_clean_architecture.core.gateway;

import com.rlevi.studying_clean_architecture.core.entities.RefreshToken;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenGateway {
  RefreshToken save(RefreshToken refreshToken);
  Optional<RefreshToken> findByToken(String token);
  Optional<RefreshToken> findByTokenAndRevokedFalse(String token);
  List<RefreshToken> findByUserId(Long userId);
  void revokeByToken(String token, String replacementToken);
  void deleteByUserId(Long userId);
}
