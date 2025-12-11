package com.rlevi.studying_clean_architecture.core.entities;

import com.rlevi.studying_clean_architecture.core.enums.Role;

public record User(
        Long id,
        String email,
        String name,
        String passwordHash,
        Role role
) {
}
