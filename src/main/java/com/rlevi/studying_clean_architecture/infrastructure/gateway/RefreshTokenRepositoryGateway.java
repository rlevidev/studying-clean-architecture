package com.rlevi.studying_clean_architecture.infrastructure.gateway;

import com.rlevi.studying_clean_architecture.core.entities.RefreshToken;
import com.rlevi.studying_clean_architecture.core.gateway.RefreshTokenGateway;
import com.rlevi.studying_clean_architecture.infrastructure.mapper.RefreshTokenMapper;
import com.rlevi.studying_clean_architecture.infrastructure.persistence.RefreshTokenEntity;
import com.rlevi.studying_clean_architecture.infrastructure.persistence.RefreshTokenRepository;
import com.rlevi.studying_clean_architecture.infrastructure.persistence.UserEntity;
import com.rlevi.studying_clean_architecture.infrastructure.persistence.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
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
    Optional<RefreshTokenEntity> optionalEntity = refreshTokenRepository.findByToken(token);
    if (optionalEntity.isPresent()) {
      RefreshTokenEntity entity = optionalEntity.get();
      entity.setRevoked(true);
      entity.setReplacedByToken(replacementToken);
      refreshTokenRepository.save(entity);
    }
  }

  @Override
  public void deleteByUserId(Long userId) {
    refreshTokenRepository.deleteByUserId(userId);
  }
}
