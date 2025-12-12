package com.rlevi.studying_clean_architecture.core.usecases.finduserbyemail;

import com.rlevi.studying_clean_architecture.core.entities.User;

import java.util.Optional;

public interface FindUserByEmailUseCase {
  Optional<User> execute(String email);
}
