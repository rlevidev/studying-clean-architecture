package com.rlevi.studying_clean_architecture.infrastructure.mapper;

import com.rlevi.studying_clean_architecture.core.entities.RefreshToken;
import com.rlevi.studying_clean_architecture.infrastructure.persistence.RefreshTokenEntity;
import com.rlevi.studying_clean_architecture.infrastructure.persistence.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenMapper {
  public RefreshToken toDomain(RefreshTokenEntity entity) {
    return new RefreshToken(
            entity.getId(),
            entity.getToken(),
            entity.getUser().getId(),
            entity.getExpiryDate(),
            entity.getCreatedAt(),
            entity.isRevoked(),
            entity.getReplacedByToken()
    );
  }

  public RefreshTokenEntity toEntity(RefreshToken refreshToken) {
    RefreshTokenEntity entity = new RefreshTokenEntity();
    entity.setId(refreshToken.id());
    entity.setToken(refreshToken.token());
    entity.setExpiryDate(refreshToken.expiryDate());
    entity.setCreatedAt(refreshToken.createdAt());
    entity.setRevoked(refreshToken.revoked());
    entity.setReplacedByToken(refreshToken.replacedByToken());

    return entity;
  }

  public RefreshTokenEntity toEntityWithUser(RefreshToken refreshToken, UserEntity user) {
    RefreshTokenEntity entity = toEntity(refreshToken);
    entity.setUser(user);

    return entity;
  }
}
