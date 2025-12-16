package com.rlevi.studying_clean_architecture.infrastructure.dto.login;

public record UserLoginRequest(
        String email,
        String password
) {
}
