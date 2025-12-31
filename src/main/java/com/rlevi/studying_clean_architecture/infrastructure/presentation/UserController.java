package com.rlevi.studying_clean_architecture.infrastructure.presentation;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.usecases.createuser.CreateUserUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.findallusers.FindAllUsersUseCase;
import com.rlevi.studying_clean_architecture.core.usecases.finduserbyid.FindUserByIdUseCase;
import com.rlevi.studying_clean_architecture.infrastructure.dto.login.UserLoginRequest;
import com.rlevi.studying_clean_architecture.infrastructure.dto.login.UserLoginResponse;
import com.rlevi.studying_clean_architecture.infrastructure.dto.register.UserRegisterRequest;
import com.rlevi.studying_clean_architecture.infrastructure.dto.register.UserRegisterResponse;
import com.rlevi.studying_clean_architecture.infrastructure.dto.response.UserResponse;
import com.rlevi.studying_clean_architecture.infrastructure.mapper.UserMapper;
import com.rlevi.studying_clean_architecture.infrastructure.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
  private final CreateUserUseCase createUserUseCase;
  private final FindAllUsersUseCase findAllUsersUseCase;
  private final FindUserByIdUseCase findUserByIdUseCase;
  private final UserMapper userMapper;
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;

  public UserController(
          CreateUserUseCase createUserUseCase,
          FindAllUsersUseCase findAllUsersUseCase,
          FindUserByIdUseCase findUserByIdUseCase,
          UserMapper userMapper,
          AuthenticationManager authenticationManager,
          JwtUtil jwtUtil) {
    this.createUserUseCase = createUserUseCase;
    this.findAllUsersUseCase = findAllUsersUseCase;
    this.findUserByIdUseCase = findUserByIdUseCase;
    this.userMapper = userMapper;
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
  }

  // Create user
  @PostMapping("/register")
  public ResponseEntity<UserRegisterResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
    User userToCreate = userMapper.toDomain(request);
    createUserUseCase.execute(userToCreate);
    String token = jwtUtil.generateToken(request.email(), "ROLE_USER");

    return ResponseEntity.ok(UserRegisterResponse.success(token));
  }

  // User login
  @PostMapping("/login")
  public ResponseEntity<UserLoginResponse> loginUser(@Valid @RequestBody UserLoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
    );

    if (!authentication.isAuthenticated()) {
      throw new BadCredentialsException("Authentication failed");
    }

    // Gets the role of the authenticated user
    String role = authentication.getAuthorities().stream()
            .findFirst()
            .map(GrantedAuthority::getAuthority)
            .orElse("ROLE_USER");
    
    // Generates the JWT token
    String token = jwtUtil.generateToken(request.email(), role);
    
    return ResponseEntity.ok(UserLoginResponse.success(token));
  }

  @GetMapping("/all")
  public ResponseEntity<List<UserResponse>> getAllUsers() {
    List<User> users = findAllUsersUseCase.execute();
    List<UserResponse> response = users.stream()
            .map(userMapper::toResponse)
            .collect(Collectors.toList());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
    Optional<User> user = findUserByIdUseCase.execute(id);
    UserResponse response = user.map(userMapper::toResponse)
            .orElse(null);

    return ResponseEntity.ok(response);
  }
}