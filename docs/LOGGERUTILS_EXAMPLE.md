# Exemplo de Uso do LoggerUtils

## Visão Geral

Este documento demonstra como usar o LoggerUtils em diferentes camadas do projeto.

## Exemplo 1: Use Case com Logging

```java
package com.rlevi.studying_clean_architecture.core.usecases.createuser;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.exception.UserAlreadyExistsException;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;
import com.rlevi.studying_clean_architecture.core.gateway.PasswordEncoderGateway;
import com.rlevi.studying_clean_architecture.core.utils.DomainValidator;
import com.rlevi.studying_clean_architecture.core.utils.LoggerUtils;
import org.slf4j.Logger;

import java.util.Map;

public class CreateUserUseCaseImpl implements CreateUserUseCase {
  private final UserGateway userGateway;
  private final PasswordEncoderGateway passwordEncoderGateway;

  // Adicionar logger estático
  private static final Logger logger = LoggerUtils.getLogger(CreateUserUseCaseImpl.class);

  public CreateUserUseCaseImpl(UserGateway userGateway, PasswordEncoderGateway passwordEncoderGateway) {
    this.userGateway = userGateway;
    this.passwordEncoderGateway = passwordEncoderGateway;
  }

  @Override
  public User execute(User user) {
    // Log de início da operação
    LoggerUtils.logDebug(logger, "Creating user",
            Map.of("email", user.email(), "name", user.name()));

    try {
      // Validação de email
      LoggerUtils.logDebug(logger, "Validating email format", Map.of("email", user.email()));
      DomainValidator.validateEmail(user.email());
      LoggerUtils.logValidation(logger, "email", true, user.email());

      // Validação de nome
      LoggerUtils.logDebug(logger, "Validating name", Map.of("name", user.name()));
      DomainValidator.validateName(user.name());
      LoggerUtils.logValidation(logger, "name", true, user.name());

      // Verificação de existência de email
      LoggerUtils.logDebug(logger, "Checking if email already exists", Map.of("email", user.email()));
      boolean emailExists = userGateway.verifyExistsByEmail(user.email());

      if (emailExists) {
        LoggerUtils.logWarning(logger, "User already exists", Map.of("email", user.email()));
        throw new UserAlreadyExistsException("The email provided is already in use. Please use another email or log in.");
      }

      // Criptografia de senha
      LoggerUtils.logDebug(logger, "Encoding password");
      String encryptedPassword = passwordEncoderGateway.encode(user.passwordHash());

      // Criação do objeto de usuário para salvar
      LoggerUtils.logDebug(logger, "Creating user object for database");
      var userToSave = new User(
              null,
              user.email(),
              user.name(),
              encryptedPassword,
              Role.USER,
              null,
              null
      );

      // Salvamento no banco de dados
      LoggerUtils.logDebug(logger, "Saving user to database", Map.of("email", userToSave.email()));
      User savedUser = userGateway.createUser(userToSave);

      // Log de sucesso
      LoggerUtils.logSuccess(logger, "User created successfully",
              Map.of("userId", savedUser.id(), "email", savedUser.email()));

      return savedUser;

    } catch (UserAlreadyExistsException e) {
      // Log de erro específico para caso de usuário já existente
      LoggerUtils.logError(logger, "User creation failed - email already exists", e,
              Map.of("email", user.email()));
      throw e;
    } catch (Exception e) {
      // Log de erro genérico
      LoggerUtils.logError(logger, "User creation failed with unexpected error", e,
              Map.of("email", user.email()));
      throw e;
    }
  }
}
```

## Exemplo 2: Controller com Logging

```java
package com.rlevi.studying_clean_architecture.infrastructure.presentation;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.usecases.createuser.CreateUserUseCase;
import com.rlevi.studying_clean_architecture.infrastructure.dto.register.UserRegisterRequest;
import com.rlevi.studying_clean_architecture.infrastructure.dto.register.UserRegisterResponse;
import com.rlevi.studying_clean_architecture.infrastructure.mapper.UserMapper;
import com.rlevi.studying_clean_architecture.infrastructure.security.JwtUtil;
import com.rlevi.studying_clean_architecture.core.utils.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private static final Logger logger = LoggerUtils.getLogger(UserController.class);
    
    private final CreateUserUseCase createUserUseCase;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserController(CreateUserUseCase createUserUseCase, UserMapper userMapper, JwtUtil jwtUtil) {
        this.createUserUseCase = createUserUseCase;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> registerUser(@RequestBody UserRegisterRequest request) {
        // Iniciar log de requisição
        LoggerUtils.startRequest(logger, "POST /api/v1/users/register", request.email());
        
        try {
            // Log de entrada
            LoggerUtils.logDebug(logger, "Registering user", 
                Map.of("email", request.email(), "name", request.name()));
            
            // Execução da lógica de negócio
            User userToCreate = userMapper.toDomain(request);
            User createdUser = createUserUseCase.execute(userToCreate);
            
            // Geração do token JWT
            String token = jwtUtil.generateToken(request.email(), "ROLE_USER");
            
            // Log de sucesso
            LoggerUtils.logSuccess(logger, "User registered successfully", 
                Map.of("userId", createdUser.id(), "email", createdUser.email()));
            
            return ResponseEntity.ok(UserRegisterResponse.success(token));
            
        } catch (Exception e) {
            // Log de erro
            LoggerUtils.logError(logger, "Failed to register user", e, 
                Map.of("email", request.email()));
            throw e;
        } finally {
            // Finalizar log de requisição
            LoggerUtils.endRequest(logger);
        }
    }
}
```

## Exemplo 3: Gateway com Logging

```java
package com.rlevi.studying_clean_architecture.infrastructure.gateway;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;
import com.rlevi.studying_clean_architecture.infrastructure.mapper.UserMapper;
import com.rlevi.studying_clean_architecture.infrastructure.persistence.UserRepository;
import com.rlevi.studying_clean_architecture.core.utils.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepositoryGateway implements UserGateway {
    private static final Logger logger = LoggerUtils.getLogger(UserRepositoryGateway.class);
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserRepositoryGateway(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User createUser(User user) {
        LoggerUtils.logDebug(logger, "Creating user in database", Map.of("email", user.email()));
        
        try {
            UserEntity userEntity = userMapper.toEntity(user);
            UserEntity savedEntity = userRepository.save(userEntity);
            User savedUser = userMapper.toDomain(savedEntity);
            
            LoggerUtils.logSuccess(logger, "User saved to database", 
                Map.of("userId", savedUser.id(), "email", savedUser.email()));
            
            return savedUser;
            
        } catch (Exception e) {
            LoggerUtils.logError(logger, "Failed to save user to database", e, 
                Map.of("email", user.email()));
            throw e;
        }
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        LoggerUtils.logDebug(logger, "Finding user by email", Map.of("email", email));
        
        try {
            Optional<UserEntity> userEntity = userRepository.findByEmail(email);
            Optional<User> user = userEntity.map(userMapper::toDomain);
            
            if (user.isPresent()) {
                LoggerUtils.logDebug(logger, "User found", 
                    Map.of("email", email, "userId", user.get().id()));
            } else {
                LoggerUtils.logDebug(logger, "User not found", Map.of("email", email));
            }
            
            return user;
            
        } catch (Exception e) {
            LoggerUtils.logError(logger, "Failed to find user by email", e, Map.of("email", email));
            throw e;
        }
    }
}
```

## Exemplo 4: Handler de Exceção com Logging

```java
package com.rlevi.studying_clean_architecture.infrastructure.exception;

import com.rlevi.studying_clean_architecture.infrastructure.dto.ErrorResponse;
import com.rlevi.studying_clean_architecture.core.exception.UserAlreadyExistsException;
import com.rlevi.studying_clean_architecture.core.exception.AuthenticationException;
import com.rlevi.studying_clean_architecture.core.utils.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerUtils.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex, HttpServletRequest request) {
        
        LoggerUtils.logWarning(logger, "User already exists", 
            Map.of("message", ex.getMessage(), "uri", request.getRequestURI()));
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "User Already Exists",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        
        LoggerUtils.logWarning(logger, "Authentication failed", 
            Map.of("message", ex.getMessage(), "uri", request.getRequestURI()));
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication Failed",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        LoggerUtils.logError(logger, "Unexpected error occurred", ex, 
            Map.of("uri", request.getRequestURI(), "exception", ex.getClass().getSimpleName()));
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

## Exemplo 5: Uso Simples em Qualquer Classe

```java
package com.rlevi.studying_clean_architecture.example;

import com.rlevi.studying_clean_architecture.core.utils.LoggerUtils;
import org.slf4j.Logger;

import java.util.Map;

public class ExampleService {
    private static final Logger logger = LoggerUtils.getLogger(ExampleService.class);
    
    public void performOperation(String input) {
        // Logging de início
        LoggerUtils.logDebug(logger, "Starting operation", Map.of("input", input));
        
        // Validação
        LoggerUtils.logValidation(logger, "input", true, input);
        
        // Operação
        String result = processInput(input);
        
        // Logging de sucesso
        LoggerUtils.logSuccess(logger, "Operation completed", Map.of("result", result));
    }
    
    private String processInput(String input) {
        // Simulação de processamento
        return input.toUpperCase();
    }
}
```

## Níveis de Log e Quando Usar

### DEBUG
- Detalhes de fluxo de execução
- Validações de entrada
- Consultas ao banco de dados
- Operações internas

```java
LoggerUtils.logDebug(logger, "Validating email format", Map.of("email", email));
```

### INFO
- Operações bem-sucedidas
- Início/fim de requisições
- Operações críticas concluídas

```java
LoggerUtils.logSuccess(logger, "User registered successfully", Map.of("userId", userId));
```

### WARN
- Validações que falham (mas não impedem a operação)
- Tentativas de acesso negado
- Condições inesperadas mas não críticas

```java
LoggerUtils.logWarning(logger, "User already exists", Map.of("email", email));
```

### ERROR
- Exceções que impedem a operação
- Falhas de banco de dados
- Erros de negócio críticos

```java
LoggerUtils.logError(logger, "Failed to save user", exception, Map.of("email", email));
```

## Formato dos Logs

### Com MDC (Requisições)
```
2024-01-10 17:00:00.000 [550e8400-e29b-41d4-a716-446655440000] [http-nio-8080-exec-1] INFO  c.r.s.i.p.UserController - Request started - POST /api/v1/users/register
2024-01-10 17:00:00.001 [550e8400-e29b-41d4-a716-446655440000] [http-nio-8080-exec-1] DEBUG c.r.s.c.u.c.CreateUserUseCaseImpl - Creating user - {email=user@example.com, name=John Doe}
2024-01-10 17:00:00.002 [550e8400-e29b-41d4-a716-446655440000] [http-nio-8080-exec-1] INFO  c.r.s.c.u.c.CreateUserUseCaseImpl - User created successfully - {userId=123, email=user@example.com}
2024-01-10 17:00:00.003 [550e8400-e29b-41d4-a716-446655440000] [http-nio-8080-exec-1] INFO  c.r.s.i.p.UserController - Request completed - POST /api/v1/users/register
```

### Sem MDC (Operações internas)
```
2024-01-10 17:00:00.000 [http-nio-8080-exec-1] DEBUG c.r.s.c.u.c.CreateUserUseCaseImpl - Creating user - {email=user@example.com, name=John Doe}
2024-01-10 17:00:00.001 [http-nio-8080-exec-1] INFO  c.r.s.c.u.c.CreateUserUseCaseImpl - User created successfully - {userId=123, email=user@example.com}
```

## Conclusão

O LoggerUtils proporciona:
- **Consistência**: Mensagens de log padronizadas
- **Contexto**: Informações relevantes incluídas nos logs
- **Rastreamento**: Identificadores únicos para correlacionamento de requisições
- **Flexibilidade**: Diferentes níveis de log para diferentes necessidades
- **Facilidade de uso**: Métodos simples e intuitivos