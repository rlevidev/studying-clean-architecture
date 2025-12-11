package com.rlevi.studying_clean_architecture.core.gateway;

import com.rlevi.studying_clean_architecture.core.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserGateway {
  User createUser(User user);
  User updateUser(User user);
  Optional<User> findUserById(Long id);
  Optional<User> findUserByEmail(String email);
  List<User> findAllUsers();
  boolean verifyExistsByEmail(String email);
  void deleteUser(Long id);
}
