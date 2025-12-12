package com.rlevi.studying_clean_architecture.core.usecases.finduserbyid;

import com.rlevi.studying_clean_architecture.core.entities.User;

import java.util.Optional;

public interface FindUserByIdUseCase {
  Optional<User> execute(Long id);
}
