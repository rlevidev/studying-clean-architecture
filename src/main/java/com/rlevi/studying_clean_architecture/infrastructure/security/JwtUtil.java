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

  @Value("${jwt.expiration}")
  private Long expiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes());
  }

  public String generateToken(String username, String role) {
    String roleSemPrefix = role.replace("ROLE_", "");

    Instant now = Instant.now();
    Instant expirationTime = now.plus(expiration, ChronoUnit.MILLIS);

    return Jwts.builder()
            .subject(username)
            .claim("role", roleSemPrefix)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expirationTime))
            .signWith(getSigningKey())
            .compact();
  }

  public Claims extractClaims(String token) {
    return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
  }

  public String extractUsername(String token) {
    return extractClaims(token).getSubject();
  }

  public boolean isTokenExpired(String token) {
    return extractClaims(token).getExpiration().before(new Date());
  }

  public boolean validateToken(String token, String username) {
    final String extractedUsername = extractUsername(token);
    return (extractedUsername.equals(username) && !isTokenExpired(token));
  }
}
