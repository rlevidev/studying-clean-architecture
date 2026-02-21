# Sistema de Logs para Debugging e Monitoramento

## Visão Geral

Este documento descreve como implementar um sistema de logs robusto para facilitar debugging e monitoramento no projeto de autenticação Clean Architecture.

## Estratégia de Logging

### 1. Utilitário de Logs Configurável

**Arquivo:** `src/main/java/com/rlevi/studying_clean_architecture/core/utils/LoggerUtils.java`

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
     * Registra o erro de uma operação.
     */
    public static void logError(Logger logger, String message, Throwable throwable, Map<String, Object> additionalInfo) {
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            logger.error("{} - {}", message, additionalInfo, throwable);
        } else {
            logger.error(message, throwable);
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

### 2. Logs nos Controllers

**Arquivo:** `src/main/java/com/rlevi/studying_clean_architecture/infrastructure/presentation/UserController.java`

```java
// Adicionar no início da classe
private static final Logger logger = LoggerUtils.getLogger(UserController.class);

// Exemplo de implementação no método registerUser
@PostMapping("/register")
public ResponseEntity<UserRegisterResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
    LoggerUtils.startRequest(logger, "POST /api/v1/users/register", request.email());
    
    try {
        LoggerUtils.logDebug(logger, "Registering user", Map.of("email", request.email(), "name", request.name()));
        
        User userToCreate = userMapper.toDomain(request);
        User createdUser = createUserUseCase.execute(userToCreate);
        
        String token = jwtUtil.generateToken(request.email(), "ROLE_USER");
        
        LoggerUtils.logSuccess(logger, "User registered successfully", 
            Map.of("userId", createdUser.id(), "email", createdUser.email()));
        
        return ResponseEntity.ok(UserRegisterResponse.success(token));
        
    } catch (Exception e) {
        LoggerUtils.logError(logger, "Failed to register user", e, 
            Map.of("email", request.email()));
        throw e;
    } finally {
        LoggerUtils.endRequest(logger);
    }
}

// Exemplo de implementação no método loginUser
@PostMapping("/login")
public ResponseEntity<UserLoginResponse> loginUser(@Valid @RequestBody UserLoginRequest request) {
    LoggerUtils.startRequest(logger, "POST /api/v1/users/login", request.email());
    
    try {
        LoggerUtils.logDebug(logger, "User login attempt", Map.of("email", request.email()));
        
        User userToLogin = userMapper.toDomain(request);
        User authenticatedUser = loginUserUseCase.execute(userToLogin);
        
        String role = authenticatedUser.role().name();
        String token = jwtUtil.generateToken(request.email(), role);
        
        LoggerUtils.logSuccess(logger, "User login successful", 
            Map.of("userId", authenticatedUser.id(), "role", role));
        
        return ResponseEntity.ok(UserLoginResponse.success(token));
        
    } catch (AuthenticationException e) {
        LoggerUtils.logWarning(logger, "Authentication failed", 
            Map.of("email", request.email(), "reason", e.getMessage()));
        throw e;
    } catch (Exception e) {
        LoggerUtils.logError(logger, "Login error", e, Map.of("email", request.email()));
        throw e;
    } finally {
        LoggerUtils.endRequest(logger);
    }
}
```

### 3. Logs nos Use Cases

**Arquivo:** `src/main/java/com/rlevi/studying_clean_architecture/core/usecases/createuser/CreateUserUseCaseImpl.java`

```java
// Adicionar no início da classe
private static final Logger logger = LoggerUtils.getLogger(CreateUserUseCaseImpl.class);

@Override
public User execute(User user) {
    LoggerUtils.logDebug(logger, "Creating user", Map.of("email", user.email(), "name", user.name()));
    
    try {
        LoggerUtils.logValidation(logger, "email", true, user.email());
        DomainValidator.validateEmail(user.email());
        
        LoggerUtils.logValidation(logger, "name", true, user.name());
        DomainValidator.validateName(user.name());
        
        if (userGateway.verifyExistsByEmail(user.email())) {
            LoggerUtils.logWarning(logger, "User already exists", Map.of("email", user.email()));
            throw new UserAlreadyExistsException("The email provided is already in use. Please use another email or log in.");
        }
        
        LoggerUtils.logDebug(logger, "Encoding password");
        String encryptedPassword = passwordEncoderGateway.encode(user.passwordHash());
        
        var userToSave = new User(
                null,
                user.email(),
                user.name(),
                encryptedPassword,
                Role.USER,
                null,
                null
        );
        
        User savedUser = userGateway.createUser(userToSave);
        
        LoggerUtils.logSuccess(logger, "User created successfully", 
            Map.of("userId", savedUser.id(), "email", savedUser.email()));
        
        return savedUser;
        
    } catch (Exception e) {
        LoggerUtils.logError(logger, "Failed to create user", e, 
            Map.of("email", user.email(), "name", user.name()));
        throw e;
    }
}
```

**Arquivo:** `src/main/java/com/rlevi/studying_clean_architecture/core/usecases/loginuser/LoginUserUseCaseImpl.java`

```java
// Adicionar no início da classe
private static final Logger logger = LoggerUtils.getLogger(LoginUserUseCaseImpl.class);

@Override
public User execute(User user) {
    LoggerUtils.logDebug(logger, "User login attempt", Map.of("email", user.email()));
    
    try {
        if (user == null || user.email() == null || user.passwordHash() == null) {
            LoggerUtils.logWarning(logger, "Invalid login attempt - null values", Map.of("email", user != null ? user.email() : null));
            throw new AuthenticationException("Invalid email or password. Please try again.");
        }
        
        LoggerUtils.logDebug(logger, "Finding user by email", Map.of("email", user.email()));
        User foundUser = userGateway.findUserByEmail(user.email())
            .orElseThrow(() -> {
                LoggerUtils.logWarning(logger, "User not found", Map.of("email", user.email()));
                return new AuthenticationException("Invalid email or password. Please try again.");
            });
        
        LoggerUtils.logDebug(logger, "Validating password");
        if (!passwordEncoderGateway.matches(user.passwordHash(), foundUser.passwordHash())) {
            LoggerUtils.logWarning(logger, "Invalid password", Map.of("email", user.email()));
            throw new AuthenticationException("Invalid email or password. Please try again.");
        }
        
        LoggerUtils.logSuccess(logger, "User authenticated successfully", 
            Map.of("userId", foundUser.id(), "email", foundUser.email(), "role", foundUser.role().name()));
        
        return foundUser;
        
    } catch (Exception e) {
        LoggerUtils.logError(logger, "Login failed", e, Map.of("email", user.email()));
        throw e;
    }
}
```

### 4. Logs nos Gateways e Repositórios

**Arquivo:** `src/main/java/com/rlevi/studying_clean_architecture/infrastructure/gateway/UserRepositoryGateway.java`

```java
// Adicionar no início da classe
private static final Logger logger = LoggerUtils.getLogger(UserRepositoryGateway.class);

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
            LoggerUtils.logDebug(logger, "User found", Map.of("email", email, "userId", user.get().id()));
        } else {
            LoggerUtils.logDebug(logger, "User not found", Map.of("email", email));
        }
        
        return user;
        
    } catch (Exception e) {
        LoggerUtils.logError(logger, "Failed to find user by email", e, Map.of("email", email));
        throw e;
    }
}
```

### 5. Logs nos Handlers de Exceção

**Arquivo:** `src/main/java/com/rlevi/studying_clean_architecture/infrastructure/exception/GlobalExceptionHandler.java`

```java
// Adicionar no início da classe
private static final Logger logger = LoggerUtils.getLogger(GlobalExceptionHandler.class);

@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleRecurseNotFound(
        ResourceNotFoundException ex, HttpServletRequest request) {
    
    LoggerUtils.logWarning(logger, "Resource not found", 
        Map.of("message", ex.getMessage(), "uri", request.getRequestURI()));
    
    ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Resource Not Found",
            ex.getMessage(),
            request.getRequestURI()
    );
    
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
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
```

### 6. Configuração de Logs

**Arquivo:** `src/main/resources/application.properties`

```properties
# Logging Configuration
logging.level.com.rlevi.studying_clean_architecture=INFO
logging.level.com.rlevi.studying_clean_architecture.core=DEBUG
logging.level.com.rlevi.studying_clean_architecture.infrastructure=INFO
logging.level.org.springframework.security=INFO
logging.level.org.springframework.web=INFO

# Console logging
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{requestId:-}] [%thread] %-5level %logger{36} - %msg%n

# File logging
logging.file.name=logs/application.log
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{requestId:-}] [%thread] %-5level %logger{36} - %msg%n

# Log rotation
logging.logback.rollingpolicy.file-name-pattern=logs/application.%d{yyyy-MM-dd}.%i.log.gz
logging.logback.rollingpolicy.max-file-size=10MB
logging.logback.rollingpolicy.total-size-cap=100MB
logging.logback.rollingpolicy.max-history=30
```

**Arquivo:** `src/main/resources/logback-spring.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- Import default Spring Boot configuration -->
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <!-- Console Appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <loggerName/>
                <mdc/>
                <message/>
                <stackTrace/>
            </providers>
        </encoder>
    </appender>
    
    <!-- File Appender -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>100MB</totalSizeCap>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <loggerName/>
                <mdc/>
                <message/>
                <stackTrace/>
            </providers>
        </encoder>
    </appender>
    
    <!-- Error File Appender -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/error.log</file>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>ERROR</level>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/error.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>100MB</totalSizeCap>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <loggerName/>
                <mdc/>
                <message/>
                <stackTrace/>
            </providers>
        </encoder>
    </appender>
    
    <!-- Root logger configuration -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
        <appender-ref ref="ERROR_FILE"/>
    </root>
    
    <!-- Package-specific logging levels -->
    <logger name="com.rlevi.studying_clean_architecture.core" level="DEBUG"/>
    <logger name="com.rlevi.studying_clean_architecture.infrastructure" level="INFO"/>
    <logger name="org.springframework.security" level="INFO"/>
    <logger name="org.springframework.web" level="INFO"/>
</configuration>
```

### 7. Dependências Maven

**Arquivo:** `pom.xml` (adicionar dependências)

```xml
<dependencies>
    <!-- SLF4J API -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
    </dependency>
    
    <!-- Logback Classic (implementation of SLF4J) -->
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
    </dependency>
    
    <!-- Logstash Logback Encoder for structured logging -->
    <dependency>
        <groupId>net.logstash.logback</groupId>
        <artifactId>logstash-logback-encoder</artifactId>
        <version>7.4</version>
    </dependency>
    
    <!-- Spring Boot Starter Logging (includes SLF4J, Logback, and other logging dependencies) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-logging</artifactId>
    </dependency>
</dependencies>
```

## Níveis de Log Recomendados

### Desenvolvimento
- **DEBUG**: Detalhes de fluxo de execução, validações, consultas ao banco
- **INFO**: Operações bem-sucedidas, início/fim de requisições
- **WARN**: Validações falhas, tentativas de acesso negado
- **ERROR**: Exceções, falhas de operação

### Produção
- **INFO**: Operações críticas, início/fim de requisições
- **WARN**: Problemas que não impedem a operação
- **ERROR**: Erros que impedem a operação

## Estrutura de Logs

### Formato JSON (Recomendado para produção)
```json
{
  "timestamp": "2024-01-10T17:00:00.000Z",
  "level": "INFO",
  "logger": "com.rlevi.studying_clean_architecture.infrastructure.presentation.UserController",
  "message": "Request started - POST /api/v1/users/register",
  "mdc": {
    "requestId": "550e8400-e29b-41d4-a716-446655440000",
    "endpoint": "POST /api/v1/users/register",
    "userEmail": "user@example.com"
  }
}
```

### Formato Texto (Desenvolvimento)
```
2024-01-10 17:00:00.000 [550e8400-e29b-41d4-a716-446655440000] [http-nio-8080-exec-1] INFO  c.r.s.i.p.UserController - Request started - POST /api/v1/users/register
```

## Monitoramento e Debugging

### Identificadores de Rastreamento
- **requestId**: UUID único para cada requisição HTTP
- **userEmail**: Email do usuário (quando disponível)
- **endpoint**: Endpoint da requisição
- **executionTime**: Tempo de execução da operação

### Métricas de Performance
- Tempo de execução de operações críticas
- Consultas ao banco de dados
- Operações de autenticação

### Segurança
- Logs de acesso negado
- Tentativas de login falhas
- Operações em recursos protegidos

## Melhores Práticas

1. **Nunca logar informações sensíveis** (senhas, tokens JWT completos, dados pessoais)
2. **Usar MDC para correlacionamento** de logs em requisições
3. **Estruturar mensagens de log** de forma consistente
4. **Utilizar níveis de log apropriados** para cada situação
5. **Incluir contexto relevante** nas mensagens de log
6. **Monitorar logs de erro** para identificar problemas recorrentes

## Ferramentas de Monitoramento

### ELK Stack (Elasticsearch, Logstash, Kibana)
- Coleta e análise de logs
- Visualização de métricas
- Alertas baseados em logs

### Prometheus + Grafana
- Métricas de performance
- Monitoramento de saúde da aplicação
- Dashboards de monitoramento

### Sentry
- Monitoramento de exceções
- Rastreamento de erros
- Integração com sistemas de alerta

## Exemplos de Consultas de Logs

### Consultar logs de uma requisição específica
```bash
grep "550e8400-e29b-41d4-a716-446655440000" application.log
```

### Consultar logs de erro por endpoint
```bash
grep "ERROR.*POST /api/v1/users/login" application.log
```

### Consultar tempo de execução de operações
```bash
grep "Operation completed.*ms" application.log | grep -E "executionTime.*[0-9]+"
```

## Conclusão

Este sistema de logs proporciona:
- **Debugging eficiente** através de identificadores de rastreamento
- **Monitoramento em tempo real** das operações críticas
- **Análise de performance** através de métricas de tempo de execução
- **Auditoria de segurança** através de logs de acesso e autenticação
- **Facilidade de troubleshooting** com logs estruturados e contextualizados

A implementação gradual dos logs, começando pelos pontos críticos (controllers, use cases, handlers de exceção) e expandindo para gateways e repositórios, permite uma adoção segura e controlada do sistema de logging.