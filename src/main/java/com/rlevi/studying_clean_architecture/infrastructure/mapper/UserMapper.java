package com.rlevi.studying_clean_architecture.infrastructure.mapper;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.infrastructure.dto.login.UserLoginRequest;
import com.rlevi.studying_clean_architecture.infrastructure.dto.register.UserRegisterRequest;
import com.rlevi.studying_clean_architecture.infrastructure.persistence.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    /**
     * Converts from UserRegisterRequest to User domain entity
     */
    public User toDomain(UserRegisterRequest dto) {
        return new User(
                null,
                dto.email(),
                dto.name(),
                dto.password(),
                null // Role will be set in the use case
        );
    }

    /**
     * Converts from UserLoginRequest to User domain entity
     */
    public User toDomain(UserLoginRequest dto) {
        return new User(
                null,
                dto.email(),
                null,
                dto.password(),
                null
        );
    }

    /**
     * Converts from User domain entity to UserEntity
     */
    public UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.id());
        entity.setName(user.name());
        entity.setEmail(user.email());
        entity.setPasswordHash(user.passwordHash());
        entity.setRole(user.role());
        return entity;
    }

    /**
     * Converts from UserEntity to User domain entity
     */
    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getName(),
                entity.getPasswordHash(),
                entity.getRole()
        );
    }

    /**
     * Updates an existing entity with domain data
     */
    public void updateEntityFromDomain(User user, UserEntity entity) {
        entity.setName(user.name());
        entity.setEmail(user.email());
        entity.setRole(user.role());
        if (user.passwordHash() != null && !user.passwordHash().isBlank()) {
            entity.setPasswordHash(user.passwordHash());
        }
    }
}
