package com.rlevi.studying_clean_architecture.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Object for API validation error responses (e.g., 400 Bad Request)")
public record ErrorValidation(
        @Schema(description = "Date and time of the error", example = "2026-01-01T14:30:00")
        LocalDateTime timestamp,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "HTTP Error Description", example = "Bad Request")
        String error,

        @Schema(description = "Summary error message", example = "Validation failed for one or more fields")
        String message,

        @Schema(description = "Endpoint path where the error occurred", example = "/api/v1/users/1")
        String path,

        @Schema(description = "Map of field names and their respective validation error messages",
                example = "{\"email\": \"Invalid email format.\", \"password\": \"Password must have at least 8 characters.\"}")
        Map<String, String> fieldErrors
) {
}
