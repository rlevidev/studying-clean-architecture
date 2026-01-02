package com.rlevi.studying_clean_architecture.infrastructure.dto.response;

public record UserExistsResponse(
        String message,
        UserResponse infos
) {
}
