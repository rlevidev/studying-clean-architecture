package com.rlevi.studying_clean_architecture.infrastructure.presentation;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.usecases.createuser.CreateUserUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.loginuser.LoginUserUseCase;
import com.rlevi.studying_clean_architecture.core.utils.LoggerUtils;
import com.rlevi.studying_clean_architecture.infrastructure.dto.login.UserLoginRequest;
import com.rlevi.studying_clean_architecture.infrastructure.dto.login.UserLoginResponse;
import com.rlevi.studying_clean_architecture.infrastructure.dto.register.UserRegisterRequest;
import com.rlevi.studying_clean_architecture.infrastructure.dto.register.UserRegisterResponse;
import com.rlevi.studying_clean_architecture.infrastructure.mapper.UserMapper;
import com.rlevi.studying_clean_architecture.infrastructure.security.JwtUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {
  private static final Logger logger = LoggerUtils.getLogger(UserController.class);

  private final UserMapper userMapper;
  private final CreateUserUseCase createUserUseCase;
  private final LoginUserUseCase loginUserUseCase;
  private final JwtUtil jwtUtil;

  public AuthController(UserMapper userMapper, CreateUserUseCase createUserUseCase, LoginUserUseCase loginUserUseCase, JwtUtil jwtUtil) {
    this.userMapper = userMapper;
    this.createUserUseCase = createUserUseCase;
    this.loginUserUseCase = loginUserUseCase;
    this.jwtUtil = jwtUtil;
  }

  // Create user
  @PostMapping("/register")
  public ResponseEntity<UserRegisterResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
    LoggerUtils.startRequest(logger, "POST /api/v1/users/register", request.email());

    // Log of entrance
    LoggerUtils.logDebug(logger, "Registering user",
            Map.of("email", request.email(), "name", request.name()));

    // Business logic execution
    User userToCreate = userMapper.toDomain(request);
    User createdUser = createUserUseCase.execute(userToCreate);

    // Generates the JWT token
    String token = jwtUtil.generateAccessToken(request.email());

    // Log of success
    LoggerUtils.logSuccess(logger, "User registered successfully",
            Map.of("userId", createdUser.id(), "email", createdUser.email()));

    LoggerUtils.endRequest(logger);

    return ResponseEntity.ok(UserRegisterResponse.success(token));
  }

  // User login
  @PostMapping("/login")
  public ResponseEntity<UserLoginResponse> loginUser(@Valid @RequestBody UserLoginRequest request) {
    LoggerUtils.startRequest(logger, "POST /api/v1/users/login", request.email());

    // Log of entrance
    LoggerUtils.logDebug(logger, "Logging in user",
            Map.of("email", request.email()));

    // Business logic execution
    User userToLogin = userMapper.toDomain(request);
    User authenticatedUser = loginUserUseCase.execute(userToLogin);

    // Generates the JWT token
    String token = jwtUtil.generateAccessToken(request.email());

    LoggerUtils.logSuccess(logger, "User logged in successfully",
            Map.of("userId", authenticatedUser.id(), "email", authenticatedUser.email()));

    LoggerUtils.endRequest(logger);

    return ResponseEntity.ok(UserLoginResponse.success(token));
  }
}
