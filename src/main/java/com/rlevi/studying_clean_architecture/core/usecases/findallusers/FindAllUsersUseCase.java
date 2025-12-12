package com.rlevi.studying_clean_architecture.core.usecases.findallusers;

import com.rlevi.studying_clean_architecture.core.entities.User;

import java.util.List;

public interface FindAllUsersUseCase {
  List<User> execute();
}
