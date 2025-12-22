package com.rlevi.studying_clean_architecture.infrastructure.presentation;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.usecases.createuser.CreateUserUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.deleteuser.DeleteUserUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.findallusers.FindAllUsersUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.finduserbyemail.FindUserByEmailUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.finduserbyid.FindUserByIdUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.updateuser.UpdateUserUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.verifyexistsbyemail.VerifyExistsByEmailUseCase;
import com.rlevi.studying_clean_architecture.infrastructure.dto.login.UserLoginRequest;
import com.rlevi.studying_clean_architecture.infrastructure.dto.login.UserLoginResponse;
import com.rlevi.studying_clean_architecture.infrastructure.dto.register.UserRegisterRequest;
import com.rlevi.studying_clean_architecture.infrastructure.dto.register.UserRegisterResponse;
import com.rlevi.studying_clean_architecture.infrastructure.mapper.UserMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
  private final CreateUserUseCase createUserUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final FindUserByIdUseCase findUserByIdUseCase;
  private final FindUserByEmailUseCase findUserByEmailUseCase;
  private final FindAllUsersUseCase findAllUsersUseCase;
  private final VerifyExistsByEmailUseCase verifyExistsByEmailUseCase;
  private final DeleteUserUseCase deleteUserUseCase;
  private final UserMapper userMapper;

  public UserController(CreateUserUseCase createUserUseCase, UpdateUserUseCase updateUserUseCase, FindUserByIdUseCase findUserByIdUseCase, FindUserByEmailUseCase findUserByEmailUseCase, FindAllUsersUseCase findAllUsersUseCase, VerifyExistsByEmailUseCase verifyExistsByEmailUseCase, DeleteUserUseCase deleteUserUseCase, UserMapper userMapper) {
    this.createUserUseCase = createUserUseCase;
    this.updateUserUseCase = updateUserUseCase;
    this.findUserByIdUseCase = findUserByIdUseCase;
    this.findUserByEmailUseCase = findUserByEmailUseCase;
    this.findAllUsersUseCase = findAllUsersUseCase;
    this.verifyExistsByEmailUseCase = verifyExistsByEmailUseCase;
    this.deleteUserUseCase = deleteUserUseCase;
    this.userMapper = userMapper;
  }

  @PostMapping("/register")
  public ResponseEntity<UserRegisterResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
    try {
      User userToCreate = userMapper.toDomain(request);
      User createdUser = createUserUseCase.execute(userToCreate);

      return ResponseEntity.ok(UserRegisterResponse.success());
    } catch (IllegalArgumentException e) {
      return ResponseEntity
              .badRequest()
              .body(UserRegisterResponse.fail(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity
              .internalServerError()
              .body(UserRegisterResponse.fail(e.getMessage()));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<UserLoginResponse> loginUser(@Valid @RequestBody UserLoginRequest request) {
    try {
      User userToLogin = userMapper.toDomain(request);
      Optional<User> loggedUser = findUserByEmailUseCase.execute(userToLogin.email());

      if (loggedUser.isPresent() && loggedUser.get().passwordHash().equals(userToLogin.passwordHash())) {
        return ResponseEntity.ok(UserLoginResponse.success());
      } else {
        return ResponseEntity.badRequest().body(UserLoginResponse.fail("Invalid email or password."));
      }
    } catch (Exception e) {
      return ResponseEntity
              .internalServerError()
              .body(UserLoginResponse.fail(e.getMessage()));
    }
  }
}