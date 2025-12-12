package com.rlevi.studying_clean_architecture.core.usecases.updateuser;

import com.rlevi.studying_clean_architecture.core.entities.User;

public interface UpdateUserUseCase {
  User execute(User user);
}
