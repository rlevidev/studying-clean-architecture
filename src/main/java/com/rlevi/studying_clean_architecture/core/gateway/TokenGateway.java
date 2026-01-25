package com.rlevi.studying_clean_architecture.core.gateway;

import java.time.Instant;

public interface TokenGateway {
    String generateAccessToken(String email);
    String generateRefreshToken(String email);
    Instant extractExpiration(String token);
    String extractUsername(String token);
}
