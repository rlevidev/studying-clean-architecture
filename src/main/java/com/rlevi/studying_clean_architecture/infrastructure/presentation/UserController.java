package com.rlevi.studying_clean_architecture.infrastructure.presentation;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.usecases.createuser.CreateUserUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.deleteuser.DeleteUserUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.findallusers.FindAllUsersUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.finduserbyemail.FindUserByEmailUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.finduserbyid.FindUserByIdUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.updateuser.UpdateUserUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.verifyexistsbyemail.VerifyExistsByEmailUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.loginuser.LoginUserUseCase;
import com.rlevi.studying_clean_architecture.infrastructure.dto.login.UserLoginRequest;
import com.rlevi.studying_clean_architecture.infrastructure.dto.login.UserLoginResponse;
import com.rlevi.studying_clean_architecture.infrastructure.dto.register.UserRegisterRequest;
import com.rlevi.studying_clean_architecture.infrastructure.dto.register.UserRegisterResponse;
import com.rlevi.studying_clean_architecture.infrastructure.dto.response.UserExistsResponse;
import com.rlevi.studying_clean_architecture.infrastructure.dto.response.UserResponse;
import com.rlevi.studying_clean_architecture.infrastructure.dto.update.UserUpdateRequest;
import com.rlevi.studying_clean_architecture.infrastructure.mapper.UserMapper;
import com.rlevi.studying_clean_architecture.infrastructure.security.JwtUtil;
import com.rlevi.studying_clean_architecture.core.utils.LoggerUtils;
import org.slf4j.Logger;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserController {
  private static final Logger logger = LoggerUtils.getLogger(UserController.class);
  
  private final CreateUserUseCase createUserUseCase;
  private final FindAllUsersUseCase findAllUsersUseCase;
  private final FindUserByIdUseCase findUserByIdUseCase;
  private final FindUserByEmailUseCase findUserByEmailUseCase;
  private final VerifyExistsByEmailUseCase verifyExistsByEmailUseCase;
  private final DeleteUserUseCase deleteUserUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final LoginUserUseCase loginUserUseCase;
  private final UserMapper userMapper;
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;

  public UserController(
          CreateUserUseCase createUserUseCase,
          FindAllUsersUseCase findAllUsersUseCase,
          FindUserByIdUseCase findUserByIdUseCase,
          FindUserByEmailUseCase findUserByEmailUseCase,
          VerifyExistsByEmailUseCase verifyExistsByEmailUseCase,
          DeleteUserUseCase deleteUserUseCase,
          UpdateUserUseCase updateUserUseCase,
          LoginUserUseCase loginUserUseCase,
          UserMapper userMapper,
          AuthenticationManager authenticationManager,
          JwtUtil jwtUtil) {
    this.createUserUseCase = createUserUseCase;
    this.findAllUsersUseCase = findAllUsersUseCase;
    this.findUserByIdUseCase = findUserByIdUseCase;
    this.findUserByEmailUseCase = findUserByEmailUseCase;
    this.verifyExistsByEmailUseCase = verifyExistsByEmailUseCase;
    this.deleteUserUseCase = deleteUserUseCase;
    this.updateUserUseCase = updateUserUseCase;
    this.loginUserUseCase = loginUserUseCase;
    this.userMapper = userMapper;
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
  }

  // Create user
  @PostMapping("/register")
  public ResponseEntity<UserRegisterResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
    LoggerUtils.startRequest(logger, "POST /api/v1/users/register", request.email());
    
    // Log of entrace
    LoggerUtils.logDebug(logger, "Registering user",
        Map.of("email", request.email(), "name", request.name()));
    
    // Business logic execution
    User userToCreate = userMapper.toDomain(request);
    User createdUser = createUserUseCase.execute(userToCreate);
    
    // Generates the JWT token
    String token = jwtUtil.generateToken(request.email(), "ROLE_USER");
    
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

    // Log of entrace
    LoggerUtils.logDebug(logger, "Logging in user",
        Map.of("email", request.email()));

    // Business logic execution
    User userToLogin = userMapper.toDomain(request);
    User authenticatedUser = loginUserUseCase.execute(userToLogin);

    // Gets the role of the authenticated user
    String role = authenticatedUser.role().name();

    // Generates the JWT token
    String token = jwtUtil.generateToken(request.email(), role);

    LoggerUtils.logSuccess(logger, "User logged in successfully",
        Map.of("userId", authenticatedUser.id(), "email", authenticatedUser.email()));

    LoggerUtils.endRequest(logger);

    return ResponseEntity.ok(UserLoginResponse.success(token));
  }

  // Get all users
  @GetMapping("/all")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<UserResponse>> getAllUsers() {
    List<User> users = findAllUsersUseCase.execute();
    List<UserResponse> response = users.stream()
            .map(userMapper::toResponse)
            .collect(Collectors.toList());

    return ResponseEntity.ok(response);
  }

  // Get user by id
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
    return findUserByIdUseCase.execute(id)
            .map(userMapper::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
  }

  // Get user by email
  @GetMapping()
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> getUserByEmail(@RequestParam("email") @Email(message = "Invalid email format.") String email){
    return findUserByEmailUseCase.execute(email)
            .map(userMapper::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
  }

  // Verify if user exists by email
  @GetMapping("/verify-exists")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserExistsResponse> checkExists(@RequestParam("email") @Email(message = "Invalid email format.") String email){
    boolean exists = verifyExistsByEmailUseCase.execute(email);

    if (!exists) {
      return ResponseEntity.notFound().build();
    }

    return findUserByEmailUseCase.execute(email)
            .map(user -> {
              UserResponse userResponse = userMapper.toResponse(user);
              return new UserExistsResponse("User found", userResponse);
            })
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
  }
  
  // Delete user by id
  @DeleteMapping("/delete-user")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, String>> deleteUser(@RequestParam("id") @NotNull Long id){
    deleteUserUseCase.execute(id);
    return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
  }

  // Update user
  @PutMapping("/update")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> updateUser(@RequestParam("id") @NotNull Long id, @Valid @RequestBody UserUpdateRequest request) {
    User userToUpdate = userMapper.toDomain(id, request);
    User updatedUser = updateUserUseCase.execute(userToUpdate);
    return ResponseEntity.ok(userMapper.toResponse(updatedUser));
  }
}