package com.rlevi.studying_clean_architecture.infrastructure.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard object for API error responses")
public record ErrorResponse(
        @Schema(description = "Date and time of the error", example = "2026-01-01T14:30:00")
        LocalDateTime timestamp,

        @Schema(description = "HTTP status code", example = "404")
        int status,

        @Schema(description = "HTTP Error Description", example = "Not Found")
        String error,

        @Schema(description = "Detailed error message", example = "User with ID 1 not found")
        String message,

        @Schema(description = "Endpoint path where the error occurred", example = "/api/v1/users/1")
        String path
) {
}
