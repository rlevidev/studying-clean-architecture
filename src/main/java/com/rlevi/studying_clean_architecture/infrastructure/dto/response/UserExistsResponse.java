package com.rlevi.studying_clean_architecture.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response to check if a user exists")
public record UserExistsResponse(
        @Schema(description = "Success message", example = "User exists.")
        String message,

        @Schema(description = "User's information")
        UserResponse infos
) {
}
