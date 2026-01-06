package com.rlevi.studying_clean_architecture.core.usecases.loginuser;

import com.rlevi.studying_clean_architecture.core.entities.User;

public interface LoginUserUseCase {
    User execute(User user);
}
