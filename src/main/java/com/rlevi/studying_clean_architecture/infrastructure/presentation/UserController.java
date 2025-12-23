package com.rlevi.studying_clean_architecture.infrastructure.presentation;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.usecases.createuser.CreateUserUseCase;
import com.rlevi.studying_clean_architecture.infrastructure.dto.login.UserLoginRequest;
import com.rlevi.studying_clean_architecture.infrastructure.dto.login.UserLoginResponse;
import com.rlevi.studying_clean_architecture.infrastructure.dto.register.UserRegisterRequest;
import com.rlevi.studying_clean_architecture.infrastructure.dto.register.UserRegisterResponse;
import com.rlevi.studying_clean_architecture.infrastructure.mapper.UserMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
  private final CreateUserUseCase createUserUseCase;
  private final UserMapper userMapper;
  private final AuthenticationManager authenticationManager;

  public UserController(CreateUserUseCase createUserUseCase, UserMapper userMapper, AuthenticationManager authenticationManager) {
    this.createUserUseCase = createUserUseCase;
    this.userMapper = userMapper;
    this.authenticationManager = authenticationManager;
  }

  // Create user
  @PostMapping("/register")
  public ResponseEntity<UserRegisterResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
    try {
      User userToCreate = userMapper.toDomain(request);
      createUserUseCase.execute(userToCreate);

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

  // User login
  @PostMapping("/login")
  public ResponseEntity<UserLoginResponse> loginUser(@Valid @RequestBody UserLoginRequest request) {
    try {
      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.email(), request.password())
      );

      if (authentication.isAuthenticated()) {
        return ResponseEntity.ok(UserLoginResponse.success());
      } else {
        return ResponseEntity.badRequest().body(UserLoginResponse.fail("Invalid email or password."));
      }
    } catch (AuthenticationException e) {
      return ResponseEntity.badRequest().body(UserLoginResponse.fail("Invalid email or password."));
    } catch (Exception e) {
      return ResponseEntity
              .internalServerError()
              .body(UserLoginResponse.fail(e.getMessage()));
    }
  }
}