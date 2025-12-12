package com.rlevi.studying_clean_architecture.core.usecases.findallusers;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;

import java.util.List;

public class FindAllUsersUseCaseImpl implements FindAllUsersUseCase {
  private final UserGateway userGateway;

  public FindAllUsersUseCaseImpl(UserGateway userGateway) {
    this.userGateway = userGateway;
  }

  @Override
  public List<User> execute() {
    List<User> users = userGateway.findAllUsers();
    return users != null ? users : List.of();
  }
}
