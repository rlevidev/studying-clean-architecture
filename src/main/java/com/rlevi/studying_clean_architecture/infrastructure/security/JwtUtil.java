package com.rlevi.studying_clean_architecture.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtUtil {
  @Value("${JWT_SECRET}")
  private String secret;

  @Value("${jwt.access.expiration}")
  private Long accessTokenExpiration;

  @Value("${jwt.refresh.expiration}")
  private Long refreshTokenExpiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes());
  }

  public String generateToken(String username, Long expiration) {

    Instant now = Instant.now();
    Instant expirationTime = now.plus(expiration, ChronoUnit.MILLIS);

    return Jwts.builder()
            .subject(username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expirationTime))
            .signWith(getSigningKey())
            .compact();
  }

  public String generateAccessToken(String username) {
    return generateToken(username, accessTokenExpiration);
  }

  public String generateRefreshToken(String username) {
    return generateToken(username, refreshTokenExpiration);
  }

  public String extractUsername(String token) {
    return extractClaims(token).getSubject();
  }

  public Instant extractExpiration(String token) {
    return extractClaims(token).getExpiration().toInstant();
  }


  public Claims extractClaims(String token) {
    return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
  }

  public boolean isTokenExpired(String token) {
    return extractClaims(token).getExpiration().before(new Date());
  }

  public boolean validateToken(String token, String username) {
    final String extractedUsername = extractUsername(token);
    return (extractedUsername.equals(username) && !isTokenExpired(token));
  }

  public String generateTokenId() {
    return java.util.UUID.randomUUID().toString();
  }
}
