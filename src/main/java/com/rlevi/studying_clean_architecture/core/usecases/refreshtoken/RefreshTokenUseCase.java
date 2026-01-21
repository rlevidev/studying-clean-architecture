package com.rlevi.studying_clean_architecture.core.usecases.refreshtoken;

import com.rlevi.studying_clean_architecture.core.entities.AuthResult;

public interface RefreshTokenUseCase {
    AuthResult execute(String refreshToken);
}
