package com.rlevi.studying_clean_architecture.infrastructure.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
