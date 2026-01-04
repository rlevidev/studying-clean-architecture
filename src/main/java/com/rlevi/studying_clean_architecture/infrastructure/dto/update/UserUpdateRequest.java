package com.rlevi.studying_clean_architecture.infrastructure.dto.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters.")
        String name,

        @Email(message = "Invalid email format.")
        String email,

        @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters.")
        String password
) {
}
