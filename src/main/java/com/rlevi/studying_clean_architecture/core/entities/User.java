package com.rlevi.studying_clean_architecture.core.entities;

import com.rlevi.studying_clean_architecture.core.enums.Role;
import com.rlevi.studying_clean_architecture.core.utils.DomainValidator;

import java.time.Instant;

public record User(
        Long id,
        String email,
        String name,
        String passwordHash,
        Role role,
        Instant createdAt,
        Instant updatedAt
) {
    public User {
        DomainValidator.validateEmail(email);
        DomainValidator.validateName(name);
    }
}
