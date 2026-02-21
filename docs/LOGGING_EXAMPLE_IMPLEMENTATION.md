# Exemplo Prático: Implementação de Logs no CreateUserUseCase

## Visão Geral

Este documento demonstra como implementar logs passo-a-passo no caso de uso de criação de usuário, mostrando a transformação do código original para uma versão com logging completo.

## Código Original

```java
package com.rlevi.studying_clean_architecture.core.usecases.createuser;

import com.rlevi.studying_clean_architecture.core.entities.User;
import com.rlevi.studying_clean_architecture.core.exception.UserAlreadyExistsException;
import com.rlevi.studying_clean_architecture.core.gateway.UserGateway;
import com.rlevi.studying_clean_architecture.core.gateway.PasswordEncoderGateway;
import com.rlevi.studying_clean_architecture.core.utils.DomainValidator;

public class CreateUserUseCaseImpl implements CreateUserUseCase {
  private final UserGateway userGateway;
  private final PasswordEncoderGateway passwordEncoderGateway;

  public CreateUserUseCaseImpl(UserGateway userGateway, PasswordEncoderGateway passwordEncoderGateway) {
    this.userGateway = userGateway;
    this.passwordEncoderGateway = passwordEncoderGateway;
  }

  @Override
  public User execute(User user) {
    DomainValidator.validateEmail(user.email());
    DomainValidator.validateName(user.name());

    if (userGateway.verifyExistsByEmail(user.email())) {
      throw new UserAlreadyExistsException("The email provided is already in use. Please use another email or log in.");
    }

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

    return userGateway.createUser(userToSave);
  }
}
```

## Código com Logs Implementados

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

    long startTime = System.currentTimeMillis();

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
      long executionTime = System.currentTimeMillis() - startTime;
      LoggerUtils.logExecutionTime(logger, "User creation", executionTime,
              Map.of("userId", savedUser.id(), "email", savedUser.email()));

      return savedUser;

    } catch (UserAlreadyExistsException e) {
      // Log de erro específico para caso de usuário já existente
      LoggerUtils.logError(logger, "User creation failed - email already exists", e,
              Map.of("email", user.email()));
      throw e;
    } catch (Exception e) {
      // Log de erro genérico
      long executionTime = System.currentTimeMillis() - startTime;
      LoggerUtils.logError(logger, "User creation failed with unexpected error", e,
              Map.of("email", user.email(), "executionTime", executionTime));
      throw e;
    }
  }
}
```

## Análise das Melhorias

### 1. Identificação de Problemas no Código Original

**Problemas identificados:**
- **Falta de rastreamento**: Não é possível saber quando e como o caso de uso foi executado
- **Debugging difícil**: Em caso de erro, não há informações sobre o fluxo de execução
- **Ausência de métricas**: Não é possível medir o tempo de execução das operações
- **Validações silenciosas**: Não há registro de quando as validações são executadas
- **Operações críticas sem log**: Criptografia de senha e salvamento no banco não são registrados

### 2. Soluções Implementadas

#### a) Rastreamento de Execução
```java
// Log de início da operação
LoggerUtils.logDebug(logger, "Creating user", 
    Map.of("email", user.email(), "name", user.name()));
```

#### b) Validação com Registro
```java
// Validação de email com log
LoggerUtils.logDebug(logger, "Validating email format", Map.of("email", user.email()));
DomainValidator.validateEmail(user.email());
LoggerUtils.logValidation(logger, "email", true, user.email());
```

#### c) Métricas de Performance
```java
long startTime = System.currentTimeMillis();
// ... operações ...
long executionTime = System.currentTimeMillis() - startTime;
LoggerUtils.logExecutionTime(logger, "User creation", executionTime,
    Map.of("userId", savedUser.id(), "email", savedUser.email()));
```

#### d) Tratamento de Erros Detalhado
```java
} catch (UserAlreadyExistsException e) {
    LoggerUtils.logError(logger, "User creation failed - email already exists", e, 
        Map.of("email", user.email()));
    throw e;
} catch (Exception e) {
    LoggerUtils.logError(logger, "User creation failed with unexpected error", e, 
        Map.of("email", user.email(), "executionTime", executionTime));
    throw e;
}
```

### 3. Benefícios Obtidos

#### a) Debugging Eficiente
- **Rastreamento completo**: É possível acompanhar todo o fluxo de execução
- **Contexto rico**: Informações sobre email, nome, tempo de execução
- **Erros detalhados**: Mensagens de erro com contexto completo

#### b) Monitoramento de Performance
- **Tempo de execução**: Medição do tempo total da operação
- **Bottlenecks identificados**: É possível identificar operações lentas
- **Métricas de negócio**: Tempo médio de criação de usuários

#### c) Auditoria de Segurança
- **Operações críticas registradas**: Criptografia de senha é registrada
- **Validações auditáveis**: Todas as validações são registradas
- **Erros de segurança**: Tentativas de criar usuários duplicados são registradas

#### d) Troubleshooting Simplificado
- **Identificação rápida de problemas**: Logs estruturados facilitam a busca
- **Correlacionamento de eventos**: Uso de MDC permite correlacionar logs
- **Contexto completo**: Todas as informações necessárias estão nos logs

## Comparação de Logs Gerados

### Sem Logging (Código Original)
```
// Nenhum log gerado - impossível saber o que aconteceu
```

### Com Logging (Código Modificado)
```
2024-01-10 17:00:00.000 [DEBUG] c.r.s.c.u.c.CreateUserUseCaseImpl - Creating user - {email=user@example.com, name=John Doe}
2024-01-10 17:00:00.001 [DEBUG] c.r.s.c.u.c.CreateUserUseCaseImpl - Validating email format - {email=user@example.com}
2024-01-10 17:00:00.002 [DEBUG] c.r.s.c.u.c.CreateUserUseCaseImpl - Validation passed - email for value: user@example.com
2024-01-10 17:00:00.003 [DEBUG] c.r.s.c.u.c.CreateUserUseCaseImpl - Validating name - {name=John Doe}
2024-01-10 17:00:00.004 [DEBUG] c.r.s.c.u.c.CreateUserUseCaseImpl - Validation passed - name for value: John Doe
2024-01-10 17:00:00.005 [DEBUG] c.r.s.c.u.c.CreateUserUseCaseImpl - Checking if email already exists - {email=user@example.com}
2024-01-10 17:00:00.010 [DEBUG] c.r.s.c.u.c.CreateUserUseCaseImpl - Encoding password
2024-01-10 17:00:00.015 [DEBUG] c.r.s.c.u.c.CreateUserUseCaseImpl - Creating user object for database
2024-01-10 17:00:00.020 [DEBUG] c.r.s.c.u.c.CreateUserUseCaseImpl - Saving user to database - {email=user@example.com}
2024-01-10 17:00:00.050 [INFO] c.r.s.c.u.c.CreateUserUseCaseImpl - Operation completed - User creation (50ms) - {userId=123, email=user@example.com}
```

## Aplicação em Outros Casos de Uso

### LoginUserUseCase
```java
@Override
public User execute(User user) {
    LoggerUtils.logDebug(logger, "User login attempt", Map.of("email", user.email()));
    
    try {
        // Validação de credenciais
        // Busca no banco
        // Validação de senha
        // Geração de token
        
        LoggerUtils.logSuccess(logger, "User authenticated successfully", 
            Map.of("userId", user.id(), "role", user.role().name()));
        
        return user;
    } catch (AuthenticationException e) {
        LoggerUtils.logWarning(logger, "Authentication failed", 
            Map.of("email", user.email(), "reason", e.getMessage()));
        throw e;
    }
}
```

### FindUserByIdUseCase
```java
@Override
public Optional<User> execute(Long id) {
    LoggerUtils.logDebug(logger, "Finding user by ID", Map.of("userId", id));
    
    try {
        Optional<User> user = userGateway.findUserById(id);
        
        if (user.isPresent()) {
            LoggerUtils.logDebug(logger, "User found", Map.of("userId", id, "email", user.get().email()));
        } else {
            LoggerUtils.logDebug(logger, "User not found", Map.of("userId", id));
        }
        
        return user;
    } catch (Exception e) {
        LoggerUtils.logError(logger, "Failed to find user by ID", e, Map.of("userId", id));
        throw e;
    }
}
```

## Configuração de Níveis de Log

### Desenvolvimento
```properties
logging.level.com.rlevi.studying_clean_architecture.core.usecases.createuser=DEBUG
```

### Produção
```properties
logging.level.com.rlevi.studying_clean_architecture.core.usecases.createuser=INFO
```

## Ferramentas de Análise de Logs

### Consultas comuns para análise:

#### 1. Tempo médio de criação de usuários
```bash
grep "Operation completed.*User creation" application.log | \
grep -o "([0-9]\+ms)" | \
sed 's/[^0-9]//g' | \
awk '{sum+=$1; count++} END {print "Average:", sum/count "ms"}'
```

#### 2. Taxa de falhas na criação de usuários
```bash
total=$(grep "Creating user" application.log | wc -l)
failures=$(grep "User creation failed" application.log | wc -l)
echo "Failure rate: $(echo "scale=2; $failures * 100 / $total" | bc)%"
```

#### 3. Usuários com emails duplicados
```bash
grep "User already exists" application.log | \
grep -o "email:[^,}]*" | \
sort | uniq -c | sort -nr
```

## Conclusão

A implementação de logs no CreateUserUseCase demonstra como transformar um código "silencioso" em um sistema observável e monitorável. Os benefícios incluem:

1. **Debugging eficiente**: Identificação rápida de problemas
2. **Monitoramento de performance**: Métricas de tempo de execução
3. **Auditoria de segurança**: Registro de operações críticas
4. **Troubleshooting simplificado**: Logs estruturados e contextualizados
5. **Análise de métricas**: Dados para tomada de decisão

Esta abordagem pode ser aplicada sistematicamente a todos os casos de uso, gateways, controllers e handlers de exceção, criando um sistema de logging completo e eficiente.