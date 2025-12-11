package com.rlevi.studying_clean_architecture.core.usecases.createuser;

import com.rlevi.studying_clean_architecture.core.entities.User;

public interface CreateUserUseCase {
  User execute(User user);
}
