package com.rlevi.studying_clean_architecture.infrastructure.gateway;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;
import com.rlevi.studying_clean_architecture.infrastructure.mapper.UserMapper;
import com.rlevi.studying_clean_architecture.infrastructure.persistence.UserEntity;
import com.rlevi.studying_clean_architecture.infrastructure.persistence.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserRepositoryGateway implements UserGateway {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public UserRepositoryGateway(UserRepository userRepository, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
  }

  @Override
  public User createUser(User user) {
    UserEntity userEntity = userMapper.toEntity(user);
    UserEntity savedEntity = userRepository.save(userEntity);
    return userMapper.toDomain(savedEntity);
  }

  @Override
  public User updateUser(User user) {
    if (user.id() == null || !userRepository.existsById(user.id())) {
      throw new RuntimeException("User not found");
    }

    UserEntity userEntity = userMapper.toEntity(user);
    UserEntity updatedEntity = userRepository.save(userEntity);
    return userMapper.toDomain(updatedEntity);
  }

  @Override
  public Optional<User> findUserById(Long id) {
    if (id == null) {
      return Optional.empty();
    }

    return userRepository.findById(id)
            .map(userMapper::toDomain); // "userMapper::toDomain" is the same as "userEntity -> userMapper.toDomain(userEntity)"
  }

  @Override
  public Optional<User> findUserByEmail(String email) {
    if (email == null || email.isBlank()) {
      return Optional.empty();
    }

    return userRepository.findByEmail(email)
            .map(userMapper::toDomain);
  }

  @Override
  public List<User> findAllUsers() {
    return userRepository.findAll().stream()
            .map(userMapper::toDomain)
            .toList();
  }

  @Override
  public boolean verifyExistsByEmail(String email) {
    if (email == null || email.isBlank()) {
      return false;
    }

    return userRepository.existsByEmail(email);
  }

  @Override
  public void deleteUser(Long id) {
    if (id == null || !userRepository.existsById(id)) {
      throw new RuntimeException("User not found");
    }

    userRepository.deleteById(id);
  }
}
