package com.rlevi.studying_clean_architecture.infrastructure.gateway;

import com.rlevi.studying_clean_architecture.core.entities.RefreshToken;
import com.rlevi.studying_clean_architecture.core.gateway.RefreshTokenGateway;
import com.rlevi.studying_clean_architecture.infrastructure.mapper.RefreshTokenMapper;
import com.rlevi.studying_clean_architecture.infrastructure.persistence.RefreshTokenEntity;
import com.rlevi.studying_clean_architecture.infrastructure.persistence.RefreshTokenRepository;
import com.rlevi.studying_clean_architecture.infrastructure.persistence.UserEntity;
import com.rlevi.studying_clean_architecture.infrastructure.persistence.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class RefreshTokenRepositoryGateway implements RefreshTokenGateway {
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final RefreshTokenMapper refreshTokenMapper;

  public RefreshTokenRepositoryGateway(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, RefreshTokenMapper refreshTokenMapper) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.userRepository = userRepository;
    this.refreshTokenMapper = refreshTokenMapper;
  }

  @Override
  public RefreshToken save(RefreshToken refreshToken) {
    UserEntity userEntity = userRepository.getReferenceById(refreshToken.userId());
    RefreshTokenEntity entity = refreshTokenMapper.toEntityWithUser(refreshToken, userEntity);
    RefreshTokenEntity saved = refreshTokenRepository.save(entity);
    return refreshTokenMapper.toDomain(saved);
  }

  @Override
  public Optional<RefreshToken> findByToken(String token) {
    return refreshTokenRepository.findByToken(token)
            .map(refreshTokenMapper::toDomain);
  }

  @Override
  public Optional<RefreshToken> findByTokenAndRevokedFalse(String token) {
    return refreshTokenRepository.findByTokenAndRevokedFalse(token)
            .map(refreshTokenMapper::toDomain);
  }

  @Override
  public List<RefreshToken> findByUserId(Long userId) {
    return refreshTokenRepository.findByUserId(userId).stream()
            .map(refreshTokenMapper::toDomain)
            .toList();
  }

  @Override
  public void revokeByToken(String token, String replacementToken) {
    int updatedRows = refreshTokenRepository.revokeAndReplaceByTokenIfNotRevoked(token, replacementToken);
    if (updatedRows == 0) {
      throw new IllegalStateException("Token was already revoked by another concurrent request");
    }
  }

  @Override
  public void deleteByUserId(Long userId) {
    refreshTokenRepository.deleteByUserId(userId);
  }

  @Override
  public RefreshToken rotate(String oldToken, RefreshToken newRefreshToken) {
    // Check if old token is valid (exists and not revoked) first
    // This prevents creating a new token when the old one is already invalid
    Optional<RefreshToken> existingToken = findByTokenAndRevokedFalse(oldToken);
    if (existingToken.isEmpty()) {
      throw new IllegalStateException("Token is already revoked or invalid");
    }
    
    // Save the new token first (required for foreign key constraint)
    RefreshToken savedRefreshToken = save(newRefreshToken);
    
    // Atomically revoke the old token with the replacement link
    // This will always succeed because we already verified the token is not revoked
    revokeByToken(oldToken, savedRefreshToken.token());
    
    return savedRefreshToken;
  }
}
