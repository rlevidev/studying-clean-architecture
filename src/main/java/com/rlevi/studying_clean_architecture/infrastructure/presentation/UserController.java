package com.rlevi.studying_clean_architecture.infrastructure.presentation;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.usecases.deleteuser.DeleteUserUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.findallusers.FindAllUsersUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.finduserbyemail.FindUserByEmailUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.finduserbyid.FindUserByIdUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.verifyexistsbyemail.VerifyExistsByEmailUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.updateuser.UpdateUserUseCase;
import com.rlevi.studying_clean_architecture.infrastructure.dto.response.UserExistsResponse;
import com.rlevi.studying_clean_architecture.infrastructure.dto.response.UserResponse;
import com.rlevi.studying_clean_architecture.infrastructure.dto.update.UserUpdateRequest;
import com.rlevi.studying_clean_architecture.infrastructure.mapper.UserMapper;
import com.rlevi.studying_clean_architecture.core.utils.LoggerUtils;
import org.slf4j.Logger;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.rlevi.studying_clean_architecture.infrastructure.dto.ErrorResponse;
import com.rlevi.studying_clean_architecture.infrastructure.dto.ErrorValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@Validated
@Tag(name = "02 - Users", description = "Endpoints for user management and information retrieval")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
  private static final Logger logger = LoggerUtils.getLogger(UserController.class);

  private final FindAllUsersUseCase findAllUsersUseCase;
  private final FindUserByIdUseCase findUserByIdUseCase;
  private final FindUserByEmailUseCase findUserByEmailUseCase;
  private final VerifyExistsByEmailUseCase verifyExistsByEmailUseCase;
  private final DeleteUserUseCase deleteUserUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final UserMapper userMapper;

  public UserController(
          FindAllUsersUseCase findAllUsersUseCase,
          FindUserByIdUseCase findUserByIdUseCase,
          FindUserByEmailUseCase findUserByEmailUseCase,
          VerifyExistsByEmailUseCase verifyExistsByEmailUseCase,
          DeleteUserUseCase deleteUserUseCase,
          UpdateUserUseCase updateUserUseCase,
          UserMapper userMapper) {
    this.findAllUsersUseCase = findAllUsersUseCase;
    this.findUserByIdUseCase = findUserByIdUseCase;
    this.findUserByEmailUseCase = findUserByEmailUseCase;
    this.verifyExistsByEmailUseCase = verifyExistsByEmailUseCase;
    this.deleteUserUseCase = deleteUserUseCase;
    this.updateUserUseCase = updateUserUseCase;
    this.userMapper = userMapper;
  }

  @GetMapping("/me")
  @Operation(summary = "Get current user", description = "Retrieves the profile of the currently authenticated user")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Current user profile"),
          @ApiResponse(responseCode = "401", description = "Not authenticated",
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
    LoggerUtils.startRequest(logger, "GET /api/v1/users/me", null);

    String email = userDetails.getUsername();
    LoggerUtils.logDebug(logger, "Getting current user profile", Map.of("email", email));

    Optional<User> userOptional = findUserByEmailUseCase.execute(email);

    if (userOptional.isPresent()) {
      User user = userOptional.get();
      UserResponse userResponse = userMapper.toResponse(user);

      LoggerUtils.logSuccess(logger, "Current user profile retrieved",
          Map.of("userId", user.id(), "email", user.email()));

      LoggerUtils.endRequest(logger);
      return ResponseEntity.ok(userResponse);
    } else {
      LoggerUtils.logWarning(logger, "Authenticated user not found in database", Map.of("email", email));
      LoggerUtils.endRequest(logger);
      return ResponseEntity.notFound().build();
    }
  }

  // Get all users
  @GetMapping("/all")
  @Operation(summary = "Get all users", description = "Retrieves a list of all registered users")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
          @ApiResponse(responseCode = "403", description = "Access denied", 
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<List<UserResponse>> getAllUsers() {
    LoggerUtils.startRequest(logger, "GET /api/v1/users/all", null);

    // Log of init operation
    LoggerUtils.logDebug(logger, "Getting all users", null);

    // Business logic execution
    List<User> users = findAllUsersUseCase.execute();
    List<UserResponse> response = users.stream()
            .map(userMapper::toResponse)
            .collect(Collectors.toList());

    // Log of success
    LoggerUtils.logSuccess(logger, "Users retrieved successfully",
        Map.of("count", users.size()));

    LoggerUtils.endRequest(logger);

    return ResponseEntity.ok(response);
  }

  // Get user by id
  @GetMapping("/{id}")
  @Operation(summary = "Get user by ID", description = "Retrieves a specific user using their ID")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "User found"),
          @ApiResponse(responseCode = "404", description = "User not found", 
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
    LoggerUtils.startRequest(logger, "GET /api/v1/users/" + id, null);
    
    // Log of operation start
    LoggerUtils.logDebug(logger, "Getting user by ID", Map.of("userId", id));
    
    // Business logic execution
    Optional<User> userOptional = findUserByIdUseCase.execute(id);
    
    if (userOptional.isPresent()) {
      User user = userOptional.get();
      UserResponse userResponse = userMapper.toResponse(user);

      // Success log
      LoggerUtils.logSuccess(logger, "User found by ID",
          Map.of("userId", user.id(), "email", user.email()));
      
      LoggerUtils.endRequest(logger);
      return ResponseEntity.ok(userResponse);
    } else {
      // User not found log
      LoggerUtils.logWarning(logger, "User not found by ID", Map.of("userId", id));
      
      LoggerUtils.endRequest(logger);
      return ResponseEntity.notFound().build();
    }
  }

  // Get user by email
  @GetMapping()
  @Operation(summary = "Get user by email", description = "Retrieves a specific user using their email address")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "User found"),
          @ApiResponse(responseCode = "404", description = "User not found", 
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<UserResponse> getUserByEmail(@RequestParam("email") @Email(message = "Invalid email format.") String email){
    LoggerUtils.startRequest(logger, "GET /api/v1/users?email=" + email, null);

    // Log of operation start
    LoggerUtils.logDebug(logger, "Getting user by email", Map.of("email", email));

    // Business logic execution
    Optional<User> userOptional = findUserByEmailUseCase.execute(email);

    if (userOptional.isPresent()) {
      User user = userOptional.get();
      UserResponse userResponse = userMapper.toResponse(user);

      // Success log
      LoggerUtils.logSuccess(logger, "User found by email", Map.of("userId", user.id(), "email", user.email()));

      LoggerUtils.endRequest(logger);
      return ResponseEntity.ok(userResponse);
    } else {
      // User not found log
      LoggerUtils.logWarning(logger, "User not found by email", Map.of("email", email));

      LoggerUtils.endRequest(logger);
      return ResponseEntity.notFound().build();
    }
  }

  // Verify if user exists by email
  @GetMapping("/verify-exists")
  @Operation(summary = "Verify if user exists", description = "Checks if a user exists in the system by email")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "User exists"),
          @ApiResponse(responseCode = "404", description = "User does not exist", 
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<UserExistsResponse> checkExists(@RequestParam("email") @Email(message = "Invalid email format.") String email){
    LoggerUtils.startRequest(logger, "GET /api/v1/users/verify-exists", email);

    // Log of operation start
    LoggerUtils.logDebug(logger, "Checking if user exists by email", Map.of("email", email));

    // Business logic execution
    boolean exists = verifyExistsByEmailUseCase.execute(email);

    if (!exists) {
      LoggerUtils.logWarning(logger, "User does not exist", Map.of("email", email));
      LoggerUtils.endRequest(logger);
      return ResponseEntity.notFound().build();
    }

    Optional<User> userOptional = findUserByEmailUseCase.execute(email);

    if (userOptional.isPresent()) {
      User user = userOptional.get();
      UserResponse userResponse = userMapper.toResponse(user);

      // Success log
      LoggerUtils.logSuccess(logger, "User exists", Map.of("userId", user.id(), "email", user.email()));

      LoggerUtils.endRequest(logger);
      return ResponseEntity.ok(new UserExistsResponse("User found", userResponse));
    } else {
      // User not found log
      LoggerUtils.logWarning(logger, "User not found after exists check", Map.of("email", email));

      LoggerUtils.endRequest(logger);
      return ResponseEntity.notFound().build();
    }
  }
  
  // Delete user by id
  @DeleteMapping("/delete-user")
  @Operation(summary = "Delete user", description = "Removes a user from the system by ID")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "User deleted successfully"),
          @ApiResponse(responseCode = "404", description = "User not found", 
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<Map<String, String>> deleteUser(@RequestParam("id") @NotNull Long id){
    LoggerUtils.startRequest(logger, "DELETE /api/v1/users/delete-user?id=" + id, null);

    // Log of operation start
    LoggerUtils.logDebug(logger, "Deleting user", Map.of("userId", id));

    // Business logic execution
    deleteUserUseCase.execute(id);

    // Success log
    LoggerUtils.logSuccess(logger, "User deleted successfully", Map.of("userId", id));

    LoggerUtils.endRequest(logger);

    return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
  }

  // Update user
  @PutMapping("/update")
  @Operation(summary = "Update user", description = "Updates an existing user's information")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "User updated successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid input", 
                  content = @Content(schema = @Schema(implementation = ErrorValidation.class))),
          @ApiResponse(responseCode = "404", description = "User not found", 
                  content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<UserResponse> updateUser(@RequestParam("id") @NotNull Long id, @Valid @RequestBody UserUpdateRequest request) {
    LoggerUtils.startRequest(logger, "PUT /api/v1/users/update?id=" + id, null);

    // Log of operation start
    LoggerUtils.logDebug(logger, "Updating user", Map.of("userId", id));

    // Business logic execution
    User userToUpdate = userMapper.toDomain(id, request);
    User updatedUser = updateUserUseCase.execute(userToUpdate);

    // Success log
    LoggerUtils.logSuccess(logger, "User updated successfully", Map.of("userId", id));

    LoggerUtils.endRequest(logger);

    return ResponseEntity.ok(userMapper.toResponse(updatedUser));
  }
}