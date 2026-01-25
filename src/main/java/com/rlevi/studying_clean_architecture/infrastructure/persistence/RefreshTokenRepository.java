package com.rlevi.studying_clean_architecture.infrastructure.persistence;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Transactional
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
  Optional<RefreshTokenEntity> findByToken(String token);
  Optional<RefreshTokenEntity> findByTokenAndRevokedFalse(String token);
  List<RefreshTokenEntity> findByUserId(Long userId);
  
  @Modifying
  void deleteByUserId(Long userId);
  
  @Modifying
  @Query("UPDATE RefreshTokenEntity r SET r.revoked = true, r.replacedByToken = :replacementToken WHERE r.token = :token AND r.revoked = false")
  int revokeAndReplaceByTokenIfNotRevoked(@Param("token") String token, @Param("replacementToken") String replacementToken);
}
