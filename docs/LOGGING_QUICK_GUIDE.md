# Guia Rápido de Logging

## Visão Geral

Este guia fornece exemplos práticos e rápidos de como implementar e usar o sistema de logs no projeto.

## Como Começar

### 1. Importar o LoggerUtils

```java
import com.rlevi.studying_clean_architecture.core.utils.LoggerUtils;
import org.slf4j.Logger;
```

### 2. Criar um Logger na Classe

```java
public class ExemploController {
    private static final Logger logger = LoggerUtils.getLogger(ExemploController.class);
    
    // Restante da classe...
}
```

## Exemplos Práticos

### Controller Básico

```java
@RestController
@RequestMapping("/api/v1/exemplo")
public class ExemploController {
    private static final Logger logger = LoggerUtils.getLogger(ExemploController.class);
    
    @PostMapping("/criar")
    public ResponseEntity<String> criar(@RequestBody String dado) {
        LoggerUtils.startRequest(logger, "POST /api/v1/exemplo/criar", null);
        
        try {
            LoggerUtils.logDebug(logger, "Processando requisição", Map.of("dado", dado));
            
            // Processamento...
            String resultado = processarDado(dado);
            
            LoggerUtils.logSuccess(logger, "Requisição processada com sucesso", 
                Map.of("resultado", resultado));
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            LoggerUtils.logError(logger, "Erro ao processar requisição", e, 
                Map.of("dado", dado));
            throw e;
        } finally {
            LoggerUtils.endRequest(logger);
        }
    }
}
```

### Use Case com Validação

```java
public class ExemploUseCaseImpl implements ExemploUseCase {
    private static final Logger logger = LoggerUtils.getLogger(ExemploUseCaseImpl.class);
    
    @Override
    public Resultado execute(Entrada entrada) {
        LoggerUtils.logDebug(logger, "Iniciando processamento", 
            Map.of("campo1", entrada.getCampo1()));
        
        try {
            // Validações
            LoggerUtils.logValidation(logger, "campo1", true, entrada.getCampo1());
            validarCampo1(entrada.getCampo1());
            
            LoggerUtils.logValidation(logger, "campo2", true, entrada.getCampo2());
            validarCampo2(entrada.getCampo2());
            
            // Processamento
            Resultado resultado = processar(entrada);
            
            LoggerUtils.logSuccess(logger, "Processamento concluído", 
                Map.of("id", resultado.getId()));
            
            return resultado;
            
        } catch (ValidacaoException e) {
            LoggerUtils.logWarning(logger, "Validação falhou", 
                Map.of("campo", e.getCampo(), "valor", e.getValor()));
            throw e;
        } catch (Exception e) {
            LoggerUtils.logError(logger, "Erro inesperado", e, 
                Map.of("campo1", entrada.getCampo1()));
            throw e;
        }
    }
}
```

### Gateway com Operações de Banco

```java
public class ExemploGatewayImpl implements ExemploGateway {
    private static final Logger logger = LoggerUtils.getLogger(ExemploGatewayImpl.class);
    
    @Override
    public Optional<Entidade> buscarPorId(Long id) {
        LoggerUtils.logDebug(logger, "Buscando entidade por ID", Map.of("id", id));
        
        try {
            Optional<Entidade> entidade = repository.findById(id);
            
            if (entidade.isPresent()) {
                LoggerUtils.logDebug(logger, "Entidade encontrada", 
                    Map.of("id", id, "nome", entidade.get().getNome()));
            } else {
                LoggerUtils.logDebug(logger, "Entidade não encontrada", Map.of("id", id));
            }
            
            return entidade;
            
        } catch (Exception e) {
            LoggerUtils.logError(logger, "Erro ao buscar entidade", e, Map.of("id", id));
            throw e;
        }
    }
    
    @Override
    public Entidade salvar(Entidade entidade) {
        LoggerUtils.logDebug(logger, "Salvando entidade", 
            Map.of("id", entidade.getId(), "nome", entidade.getNome()));
        
        try {
            Entidade salva = repository.save(entidade);
            
            LoggerUtils.logSuccess(logger, "Entidade salva com sucesso", 
                Map.of("id", salva.getId()));
            
            return salva;
            
        } catch (Exception e) {
            LoggerUtils.logError(logger, "Erro ao salvar entidade", e, 
                Map.of("id", entidade.getId()));
            throw e;
        }
    }
}
```

### Handler de Exceção

```java
@RestControllerAdvice
public class ExemploExceptionHandler {
    private static final Logger logger = LoggerUtils.getLogger(ExemploExceptionHandler.class);
    
    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<ErrorResponse> handleValidacaoException(
            ValidacaoException ex, HttpServletRequest request) {
        
        LoggerUtils.logWarning(logger, "Validação falhou", 
            Map.of("campo", ex.getCampo(), "valor", ex.getValor(), "uri", request.getRequestURI()));
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        LoggerUtils.logError(logger, "Erro inesperado", ex, 
            Map.of("uri", request.getRequestURI(), "classe", ex.getClass().getSimpleName()));
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "Ocorreu um erro inesperado. Por favor, tente novamente.",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

## Níveis de Log

### DEBUG
Use para:
- Detalhes de fluxo de execução
- Validações de entrada
- Consultas ao banco de dados
- Operações internas

```java
LoggerUtils.logDebug(logger, "Processando requisição", Map.of("parametro", valor));
```

### INFO
Use para:
- Operações bem-sucedidas
- Início/fim de requisições
- Operações críticas concluídas

```java
LoggerUtils.logSuccess(logger, "Usuário criado com sucesso", Map.of("id", userId));
```

### WARN
Use para:
- Validações que falham (mas não impedem a operação)
- Tentativas de acesso negado
- Condições inesperadas mas não críticas

```java
LoggerUtils.logWarning(logger, "Tentativa de acesso negado", Map.of("recurso", recurso, "role", role));
```

### ERROR
Use para:
- Exceções que impedem a operação
- Falhas de banco de dados
- Erros de negócio críticos

```java
LoggerUtils.logError(logger, "Falha ao salvar no banco", exception, Map.of("tabela", "usuarios"));
```

## Mensagens de Log Comuns

### Operações CRUD
```java
// Create
LoggerUtils.logSuccess(logger, "Registro criado com sucesso", Map.of("id", id));

// Read
LoggerUtils.logDebug(logger, "Registro encontrado", Map.of("id", id, "campo", valor));

// Update
LoggerUtils.logSuccess(logger, "Registro atualizado", Map.of("id", id));

// Delete
LoggerUtils.logSuccess(logger, "Registro excluído", Map.of("id", id));
```

### Autenticação
```java
// Login
LoggerUtils.logSuccess(logger, "Login bem-sucedido", Map.of("email", email, "role", role));
LoggerUtils.logWarning(logger, "Login falhou", Map.of("email", email, "motivo", motivo));

// Acesso
LoggerUtils.logAccess(logger, "recurso", true, "ADMIN");
LoggerUtils.logAccess(logger, "recurso", false, "USER");
```

### Validação
```java
LoggerUtils.logValidation(logger, "email", true, "user@example.com");
LoggerUtils.logValidation(logger, "email", false, "email-invalido");
```

### Performance
```java
long startTime = System.currentTimeMillis();
// ... operação ...
long executionTime = System.currentTimeMillis() - startTime;
LoggerUtils.logExecutionTime(logger, "Operação", executionTime, Map.of("parametros", parametros));
```

## Configuração Rápida

### application.properties
```properties
# Níveis de log
logging.level.com.rlevi.studying_clean_architecture=INFO
logging.level.com.rlevi.studying_clean_architecture.core=DEBUG

# Formato de console
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{requestId:-}] %-5level %logger{36} - %msg%n

# Arquivo de log
logging.file.name=logs/application.log
```

### logback-spring.xml (básico)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

## Consultas Comuns

### Consultar logs de erro
```bash
grep "ERROR" logs/application.log
```

### Consultar logs de uma requisição específica
```bash
grep "550e8400-e29b-41d4-a716-446655440000" logs/application.log
```

### Consultar tempo de execução
```bash
grep "Operation completed.*ms" logs/application.log
```

### Consultar logs por endpoint
```bash
grep "POST /api/v1/users" logs/application.log
```

## Dicas e Truques

### 1. Use MDC para correlacionamento
```java
LoggerUtils.startRequest(logger, "POST /api/v1/endpoint", "user@example.com");
// Todos os logs subsequentes terão requestId, endpoint e userEmail
```

### 2. Evite logar informações sensíveis
```java
// ❌ ERRADO
LoggerUtils.logDebug(logger, "Login", Map.of("email", email, "password", password));

// ✅ CERTO
LoggerUtils.logDebug(logger, "Login attempt", Map.of("email", email));
```

### 3. Use lazy evaluation para mensagens complexas
```java
// ❌ ERRADO - sempre executa a concatenação
logger.debug("Resultado: " + objeto.toString() + " com " + lista.size() + " itens");

// ✅ CERTO - só executa se o nível DEBUG estiver habilitado
if (logger.isDebugEnabled()) {
    logger.debug("Resultado: {} com {} itens", objeto.toString(), lista.size());
}
```

### 4. Estruture mensagens consistentemente
```java
// ✅ PADRÃO CONSISTENTE
LoggerUtils.logSuccess(logger, "Operação concluída", Map.of("id", id, "status", "sucesso"));
LoggerUtils.logError(logger, "Operação falhou", exception, Map.of("id", id, "status", "erro"));
```

### 5. Teste diferentes níveis de log
```bash
# Testar com DEBUG
./mvnw spring-boot:run -Dlogging.level.com.rlevi.studying_clean_architecture.core=DEBUG

# Testar com INFO
./mvnw spring-boot:run -Dlogging.level.com.rlevi.studying_clean_architecture=INFO
```

## Erros Comuns

### 1. Esquecer de limpar o MDC
```java
// ❌ ERRADO - MDC não é limpo
LoggerUtils.startRequest(logger, "endpoint", "user");
// ... operação ...
// MDC permanece com os valores

// ✅ CERTO - MDC é limpo
LoggerUtils.startRequest(logger, "endpoint", "user");
try {
    // ... operação ...
} finally {
    LoggerUtils.endRequest(logger); // Limpa o MDC
}
```

### 2. Logar exceções sem contexto
```java
// ❌ ERRADO - sem contexto
logger.error("Erro", exception);

// ✅ CERTO - com contexto
LoggerUtils.logError(logger, "Erro ao processar usuário", exception, 
    Map.of("email", email, "operacao", "criacao"));
```

### 3. Usar níveis de log incorretos
```java
// ❌ ERRADO - usar ERROR para validação que falha mas não impede a operação
LoggerUtils.logError(logger, "Email inválido", null, Map.of("email", email));

// ✅ CERTO - usar WARN para validação que falha mas não impede a operação
LoggerUtils.logWarning(logger, "Email inválido", Map.of("email", email));
```

## Conclusão

Este guia rápido fornece os fundamentos para implementar logging eficiente no projeto. Lembre-se de:

1. **Consistência**: Use mensagens e formatos consistentes
2. **Contexto**: Sempre inclua informações relevantes
3. **Segurança**: Nunca logue informações sensíveis
4. **Performance**: Use lazy evaluation e níveis de log apropriados
5. **Monitoramento**: Estruture logs para análise automatizada