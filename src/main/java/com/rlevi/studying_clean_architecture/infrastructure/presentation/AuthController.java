package com.rlevi.studying_clean_architecture.infrastructure.presentation;

import com.rlevi.studying_clean_architecture.core.entities.AuthResult;
import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.usecases.createuser.CreateUserUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.loginuser.LoginUserUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.refreshtoken.RefreshTokenUseCase;
import com.rlevi.studying_clean_architecture.core.utils.LoggerUtils;
import com.rlevi.studying_clean_architecture.infrastructure.dto.login.UserLoginRequest;
import com.rlevi.studying_clean_architecture.infrastructure.dto.login.UserLoginResponse;
import com.rlevi.studying_clean_architecture.infrastructure.dto.refreshtoken.RefreshTokenRequest;
import com.rlevi.studying_clean_architecture.infrastructure.dto.refreshtoken.RefreshTokenResponse;
import com.rlevi.studying_clean_architecture.infrastructure.dto.register.UserRegisterRequest;
import com.rlevi.studying_clean_architecture.infrastructure.dto.register.UserRegisterResponse;
import com.rlevi.studying_clean_architecture.infrastructure.mapper.UserMapper;
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
  private static final Logger logger = LoggerUtils.getLogger(AuthController.class);

  private final UserMapper userMapper;
  private final CreateUserUseCase createUserUseCase;
  private final LoginUserUseCase loginUserUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;

  public AuthController(UserMapper userMapper, CreateUserUseCase createUserUseCase, LoginUserUseCase loginUserUseCase, RefreshTokenUseCase refreshTokenUseCase) {
    this.userMapper = userMapper;
    this.createUserUseCase = createUserUseCase;
    this.loginUserUseCase = loginUserUseCase;
    this.refreshTokenUseCase = refreshTokenUseCase;
  }

  // Create user
  @PostMapping("/register")
  public ResponseEntity<UserRegisterResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
    LoggerUtils.startRequest(logger, "POST /api/v1/auth/register", request.email());

    // Log of entrance
    LoggerUtils.logDebug(logger, "Registering user",
            Map.of("email", request.email(), "name", request.name()));

    // Business logic execution
    User userToCreate = userMapper.toDomain(request);
    AuthResult authResult = createUserUseCase.execute(userToCreate);

    // Log of success
    LoggerUtils.logSuccess(logger, "User registered successfully",
            Map.of("userId", authResult.user().id(), "email", authResult.user().email()));

    LoggerUtils.endRequest(logger);

    return ResponseEntity.ok(UserRegisterResponse.success(authResult.accessToken(), authResult.refreshToken()));
  }

  // User login
  @PostMapping("/login")
  public ResponseEntity<UserLoginResponse> loginUser(@Valid @RequestBody UserLoginRequest request) {
    LoggerUtils.startRequest(logger, "POST /api/v1/auth/login", request.email());

    // Log of entrance
    LoggerUtils.logDebug(logger, "Logging in user",
            Map.of("email", request.email()));

    // Business logic execution
    User userToLogin = userMapper.toDomain(request);
    AuthResult authResult = loginUserUseCase.execute(userToLogin);

    LoggerUtils.logSuccess(logger, "User logged in successfully",
            Map.of("userId", authResult.user().id(), "email", authResult.user().email()));

    LoggerUtils.endRequest(logger);

    return ResponseEntity.ok(UserLoginResponse.success(authResult.accessToken(), authResult.refreshToken()));
  }

  // Refresh token
  @PostMapping("/refresh")
  public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
    LoggerUtils.startRequest(logger, "POST /api/v1/auth/refresh", "Token refresh operation");

    // Log of entrance
    LoggerUtils.logDebug(logger, "Refreshing token", null);

    // Business logic execution
    AuthResult authResult = refreshTokenUseCase.execute(request.refreshToken());

    // Log of success
    LoggerUtils.logSuccess(logger, "Token refreshed successfully",
            Map.of("userId", authResult.user().id(), "email", authResult.user().email()));

    LoggerUtils.endRequest(logger);

    return ResponseEntity.ok(RefreshTokenResponse.success(authResult.accessToken(), authResult.refreshToken()));
  }
}
