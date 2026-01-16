package com.rlevi.studying_clean_architecture.core.entities;

import java.time.Instant;

public record User(
        Long id,
        String email,
        String name,
        String passwordHash,
        Instant createdAt,
        Instant updatedAt
) {
}
