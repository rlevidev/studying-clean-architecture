package com.rlevi.studying_clean_architecture.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
  Optional<RefreshTokenEntity> findByToken(String token);
  Optional<RefreshTokenEntity> findByTokenAndRevokedFalse(String token);
  List<RefreshTokenEntity> findByUserId(Long userId);
  void deleteByUserId(Long userId);
}
