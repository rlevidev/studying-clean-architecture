# Sistema de Logs sem Try-Catch

## Visão Geral

Este documento apresenta uma abordagem alternativa para implementação de logs que evita o uso de try-catch, mantendo a funcionalidade de logging sem a complexidade de tratamento de exceções.

## Estratégia Alternativa

### 1. Logging Proativo (Sem Tratamento de Exceções)

Em vez de usar try-catch para capturar exceções, vamos focar em:

- **Logging de entrada e saída** de métodos
- **Logging de validações** antes da execução
- **Logging de operações críticas** durante a execução
- **Logging de métricas** de performance
- **Delegar tratamento de exceções** para o GlobalExceptionHandler

### 2. LoggerUtils Simplificado

```java
package com.rlevi.studying_clean_architecture.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

public class LoggerUtils {
    
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String USER_EMAIL_KEY = "userEmail";
    private static final String ENDPOINT_KEY = "endpoint";
    private static final String EXECUTION_TIME_KEY = "executionTime";
    
    /**
     * Inicia o log de uma requisição, configurando o MDC com informações contextuais.
     */
    public static void startRequest(Logger logger, String endpoint, String userEmail) {
        String requestId = UUID.randomUUID().toString();
        
        MDC.put(REQUEST_ID_KEY, requestId);
        MDC.put(ENDPOINT_KEY, endpoint);
        if (userEmail != null) {
            MDC.put(USER_EMAIL_KEY, userEmail);
        }
        
        logger.info("Request started - {}", endpoint);
    }
    
    /**
     * Registra o início de uma operação.
     */
    public static void startOperation(Logger logger, String operationName, Map<String, Object> context) {
        logger.info("Starting operation - {} - {}", operationName, context);
    }
    
    /**
     * Registra o sucesso de uma operação.
     */
    public static void logSuccess(Logger logger, String message, Map<String, Object> additionalInfo) {
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            logger.info("{} - {}", message, additionalInfo);
        } else {
            logger.info(message);
        }
    }
    
    /**
     * Registra um aviso (warning).
     */
    public static void logWarning(Logger logger, String message, Map<String, Object> additionalInfo) {
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            logger.warn("{} - {}", message, additionalInfo);
        } else {
            logger.warn(message);
        }
    }
    
    /**
     * Registra informação detalhada (debug).
     */
    public static void logDebug(Logger logger, String message, Map<String, Object> additionalInfo) {
        if (logger.isDebugEnabled()) {
            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                logger.debug("{} - {}", message, additionalInfo);
            } else {
                logger.debug(message);
            }
        }
    }
    
    /**
     * Finaliza o log de uma requisição, limpando o MDC.
     */
    public static void endRequest(Logger logger) {
        String requestId = MDC.get(REQUEST_ID_KEY);
        String endpoint = MDC.get(ENDPOINT_KEY);
        
        logger.info("Request completed - {}", endpoint);
        
        // Limpa o MDC
        MDC.clear();
    }
    
    /**
     * Registra o tempo de execução de uma operação.
     */
    public static void logExecutionTime(Logger logger, String operationName, long executionTimeMillis, Map<String, Object> additionalInfo) {
        MDC.put(EXECUTION_TIME_KEY, String.valueOf(executionTimeMillis));
        
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            logger.info("Operation completed - {} ({}ms) - {}", operationName, executionTimeMillis, additionalInfo);
        } else {
            logger.info("Operation completed - {} ({}ms)", operationName, executionTimeMillis);
        }
        
        MDC.remove(EXECUTION_TIME_KEY);
    }
    
    /**
     * Cria um logger para uma classe específica.
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * Registra validação de entrada de dados.
     */
    public static void logValidation(Logger logger, String validationType, boolean isValid, String value) {
        if (isValid) {
            logger.debug("Validation passed - {} for value: {}", validationType, value);
        } else {
            logger.warn("Validation failed - {} for value: {}", validationType, value);
        }
    }
    
    /**
     * Registra acesso a recursos protegidos.
     */
    public static void logAccess(Logger logger, String resource, boolean hasAccess, String userRole) {
        if (hasAccess) {
            logger.info("Access granted - Resource: {}, Role: {}", resource, userRole);
        } else {
            logger.warn("Access denied - Resource: {}, Role: {}", resource, userRole);
        }
    }
}
```

## Implementação sem Try-Catch

### 1. Controller Simplificado

```java
@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserController {
    private static final Logger logger = LoggerUtils.getLogger(UserController.class);
    
    private final CreateUserUseCase createUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        LoggerUtils.startRequest(logger, "POST /api/v1/users/register", request.email());
        
        // Logging de entrada
        LoggerUtils.logDebug(logger, "Registering user", Map.of("email", request.email(), "name", request.name()));
        
        // Execução direta (sem try-catch)
        User userToCreate = userMapper.toDomain(request);
        User createdUser = createUserUseCase.execute(userToCreate);
        
        String token = jwtUtil.generateToken(request.email(), "ROLE_USER");
        
        // Logging de sucesso
        LoggerUtils.logSuccess(logger, "User registered successfully", 
            Map.of("userId", createdUser.id(), "email", createdUser.email()));
        
        LoggerUtils.endRequest(logger);
        
        return ResponseEntity.ok(UserRegisterResponse.success(token));
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> loginUser(@Valid @RequestBody UserLoginRequest request) {
        LoggerUtils.startRequest(logger, "POST /api/v1/users/login", request.email());
        
        // Logging de entrada
        LoggerUtils.logDebug(logger, "User login attempt", Map.of("email", request.email()));
        
        // Execução direta (sem try-catch)
        User userToLogin = userMapper.toDomain(request);
        User authenticatedUser = loginUserUseCase.execute(userToLogin);
        
        String role = authenticatedUser.role().name();
        String token = jwtUtil.generateToken(request.email(), role);
        
        // Logging de sucesso
        LoggerUtils.logSuccess(logger, "User login successful", 
            Map.of("userId", authenticatedUser.id(), "role", role));
        
        LoggerUtils.endRequest(logger);
        
        return ResponseEntity.ok(UserLoginResponse.success(token));
    }
}
```

### 2. Use Case sem Try-Catch

```java
public class CreateUserUseCaseImpl implements CreateUserUseCase {
    private static final Logger logger = LoggerUtils.getLogger(CreateUserUseCaseImpl.class);
    
    private final UserGateway userGateway;
    private final PasswordEncoderGateway passwordEncoderGateway;

    @Override
    public User execute(User user) {
        // Logging de início
        LoggerUtils.startOperation(logger, "CreateUser", 
            Map.of("email", user.email(), "name", user.name()));
        
        // Validações com logging (sem try-catch)
        LoggerUtils.logValidation(logger, "email", true, user.email());
        DomainValidator.validateEmail(user.email());
        
        LoggerUtils.logValidation(logger, "name", true, user.name());
        DomainValidator.validateName(user.name());
        
        // Verificação de existência
        LoggerUtils.logDebug(logger, "Checking if email already exists", Map.of("email", user.email()));
        boolean emailExists = userGateway.verifyExistsByEmail(user.email());
        
        if (emailExists) {
            LoggerUtils.logWarning(logger, "User already exists", Map.of("email", user.email()));
            throw new UserAlreadyExistsException("The email provided is already in use. Please use another email or log in.");
        }
        
        // Criptografia de senha
        LoggerUtils.logDebug(logger, "Encoding password");
        String encryptedPassword = passwordEncoderGateway.encode(user.passwordHash());
        
        // Criação do objeto
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
        
        // Salvamento no banco
        LoggerUtils.logDebug(logger, "Saving user to database", Map.of("email", userToSave.email()));
        User savedUser = userGateway.createUser(userToSave);
        
        // Logging de sucesso
        LoggerUtils.logSuccess(logger, "User created successfully", 
            Map.of("userId", savedUser.id(), "email", savedUser.email()));
        
        return savedUser;
    }
}
```

### 3. Gateway sem Try-Catch

```java
public class UserRepositoryGateway implements UserGateway {
    private static final Logger logger = LoggerUtils.getLogger(UserRepositoryGateway.class);
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public User createUser(User user) {
        LoggerUtils.logDebug(logger, "Creating user in database", Map.of("email", user.email()));
        
        UserEntity userEntity = userMapper.toEntity(user);
        UserEntity savedEntity = userRepository.save(userEntity);
        User savedUser = userMapper.toDomain(savedEntity);
        
        LoggerUtils.logSuccess(logger, "User saved to database", 
            Map.of("userId", savedUser.id(), "email", savedUser.email()));
        
        return savedUser;
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        LoggerUtils.logDebug(logger, "Finding user by email", Map.of("email", email));
        
        Optional<UserEntity> userEntity = userRepository.findByEmail(email);
        Optional<User> user = userEntity.map(userMapper::toDomain);
        
        if (user.isPresent()) {
            LoggerUtils.logDebug(logger, "User found", Map.of("email", email, "userId", user.get().id()));
        } else {
            LoggerUtils.logDebug(logger, "User not found", Map.of("email", email));
        }
        
        return user;
    }
}
```

### 4. GlobalExceptionHandler (único lugar com try-catch)

```java
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

## Vantagens da Abordagem sem Try-Catch

### 1. Código Mais Limpo
- **Menos complexidade**: Sem blocos try-catch espalhados
- **Foco na lógica**: O código se concentra na lógica de negócio
- **Legibilidade**: Fluxo mais direto e fácil de entender

### 2. Logging Estratégico
- **Entrada e saída**: Logging nos pontos críticos
- **Validações**: Registro de validações antes da execução
- **Operações críticas**: Logging de operações importantes
- **Métricas**: Tempo de execução sem interferir no fluxo

### 3. Tratamento Centralizado
- **ExceptionHandler único**: Todas as exceções tratadas no GlobalExceptionHandler
- **Consistência**: Tratamento padronizado de exceções
- **Logging de erro**: Registro de erros sem interferir na lógica de negócio

### 4. Performance
- **Sem overhead**: Não há custo de try-catch em operações normais
- **Fluxo direto**: Execução mais rápida sem blocos de exceção
- **Logging otimizado**: Logs apenas nos pontos estratégicos

## Estratégia de Logging

### 1. Logging de Entrada
```java
LoggerUtils.startRequest(logger, "POST /api/v1/users/register", request.email());
LoggerUtils.logDebug(logger, "Registering user", Map.of("email", request.email(), "name", request.name()));
```

### 2. Logging de Validação
```java
LoggerUtils.logValidation(logger, "email", true, user.email());
DomainValidator.validateEmail(user.email());
```

### 3. Logging de Operações Críticas
```java
LoggerUtils.logDebug(logger, "Saving user to database", Map.of("email", userToSave.email()));
User savedUser = userGateway.createUser(userToSave);
```

### 4. Logging de Sucesso
```java
LoggerUtils.logSuccess(logger, "User created successfully", 
    Map.of("userId", savedUser.id(), "email", savedUser.email()));
```

### 5. Logging de Métricas
```java
long startTime = System.currentTimeMillis();
// ... operação ...
long executionTime = System.currentTimeMillis() - startTime;
LoggerUtils.logExecutionTime(logger, "User creation", executionTime,
    Map.of("userId", savedUser.id()));
```

## Configuração de Níveis de Log

### Desenvolvimento
```properties
logging.level.com.rlevi.studying_clean_architecture.core=DEBUG
logging.level.com.rlevi.studying_clean_architecture.infrastructure=INFO
```

### Produção
```properties
logging.level.com.rlevi.studying_clean_architecture=INFO
```

## Conclusão

Esta abordagem oferece:

1. **Código mais limpo** sem a complexidade de try-catch
2. **Logging estratégico** nos pontos críticos
3. **Performance melhorada** sem overhead de exceções
4. **Manutenção simplificada** com tratamento centralizado de erros
5. **Observabilidade completa** com logs bem posicionados

O sistema mantém toda a funcionalidade de logging para debugging e monitoramento, mas com uma arquitetura mais limpa e eficiente.