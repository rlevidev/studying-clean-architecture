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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.rlevi.studying_clean_architecture.infrastructure.dto.ErrorResponse;
import com.rlevi.studying_clean_architecture.infrastructure.dto.ErrorValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "01 - Authentication", description = "Endpoints for user registration, login, and token refresh")
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
  @Operation(summary = "Register a new user", description = "Creates a new user account and returns authentication tokens")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "User registered successfully", 
                  content = @Content(schema = @Schema(implementation = UserRegisterResponse.class))),
          @ApiResponse(responseCode = "400", description = "Invalid input or user already exists", 
                  content = @Content(schema = @Schema(implementation = ErrorValidation.class)))
  })
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
  @Operation(summary = "Authenticate user", description = "Authenticates user credentials and returns JWT tokens")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Login successful", 
                  content = @Content(schema = @Schema(implementation = UserLoginResponse.class))),
          @ApiResponse(responseCode = "401", description = "Invalid credentials", 
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
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
  @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid refresh token")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Token refreshed successfully", 
                  content = @Content(schema = @Schema(implementation = RefreshTokenResponse.class))),
          @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token", 
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
    LoggerUtils.startRequest(logger, "POST /api/v1/auth/refresh", null);

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
