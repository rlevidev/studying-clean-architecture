package com.rlevi.studying_clean_architecture.core.entities;

public record AuthResult(
        User user,
        String accessToken,
        String refreshToken
) {
}
