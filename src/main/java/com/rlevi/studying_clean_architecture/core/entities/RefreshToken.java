package com.rlevi.studying_clean_architecture.core.entities;

import java.time.Instant;

public record RefreshToken(
        Long id,
        String token,
        Long userId,
        Instant expiryDate,
        Instant createdAt,
        boolean revoked,
        String replacedByToken
) {
}
