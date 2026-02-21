# Plano de Melhoria: Tratamento de Exceções no UserController

## Objetivo
Melhorar o tratamento de exceções no `UserController` utilizando as melhores práticas do Spring, tornando o código mais limpo, manutenível e seguindo o princípio DRY (Don't Repeat Yourself).

## Benefícios
- Código mais limpo e organizado
- Separação de responsabilidades
- Facilidade de manutenção
- Tratamento consistente de erros em toda a aplicação
- Redução de código boilerplate

## Passos de Implementação

1. **Criar classes de exceção personalizadas**
   - `BusinessException` para erros de negócio
   - `ResourceNotFoundException` para recursos não encontrados
   - `AuthenticationException` para falhas de autenticação

2. **Criar um `@ControllerAdvice` global**
   - `GlobalExceptionHandler` para centralizar o tratamento de exceções
   - Mapear exceções para códigos de status HTTP apropriados
   - Padronizar o formato das respostas de erro

3. **Criar um DTO padrão para respostas de erro**
   ```java
   public record ErrorResponse(
       LocalDateTime timestamp,
       int status,
       String error,
       String message,
       String path
   ) {}
   ```

4. **Implementar validação de entrada**
   - Utilizar as anotações do Bean Validation
   - Criar um `@ExceptionHandler` para `MethodArgumentNotValidException`

5. **Remover os blocos try-catch dos controllers**
   - Deixar as exceções serem propagadas
   - O `@ControllerAdvice` irá capturá-las e tratá-las adequadamente

## Exemplo de Implementação

### 1. Classe de exceção personalizada
```java
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

### 2. Controller Advice
```java
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Business Error",
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication Failed",
            "Invalid email or password",
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // Outros handlers...
}
```

### 3. Controller após as melhorias
```java
@PostMapping("/register")
public ResponseEntity<UserRegisterResponse> registerUser(
    @Valid @RequestBody UserRegisterRequest request) {
    
    User userToCreate = userMapper.toDomain(request);
    createUserUseCase.execute(userToCreate);
    String token = jwtUtil.generateToken(request.email(), "ROLE_USER");

    return ResponseEntity.ok(UserRegisterResponse.success(token));
}

@PostMapping("/login")
public ResponseEntity<UserLoginResponse> loginUser(
    @Valid @RequestBody UserLoginRequest request) {
    
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password())
    );

    if (!authentication.isAuthenticated()) {
        throw new AuthenticationException("Authentication failed");
    }

    String role = authentication.getAuthorities().stream()
        .findFirst()
        .map(GrantedAuthority::getAuthority)
        .orElse("ROLE_USER");
    
    String token = jwtUtil.generateToken(request.email(), role);
    return ResponseEntity.ok(UserLoginResponse.success(token));
}
```

## Vantagens da Nova Abordagem

1. **Código mais limpo**: Remove a necessidade de blocos try-catch repetitivos
2. **Separação de preocupações**: O tratamento de erros fica centralizado
3. **Consistência**: Todas as respostas de erro seguem o mesmo formato
4. **Manutenibilidade**: Mais fácil de adicionar novos tipos de erros
5. **Testabilidade**: Mais fácil de testar tanto os casos de sucesso quanto de erro

## Próximos Passos

1. Revisar e aprovar o plano
2. Implementar as mudanças em etapas
3. Atualizar os testes unitários e de integração
4. Documentar as mudanças para a equipe

## Riscos e Mitigação

- **Risco**: Quebra de compatibilidade com clientes existentes
  - **Mitigação**: Manter o formato de resposta similar ao atual
  
- **Risco**: Dificuldade em rastrear erros
  - **Mitigação**: Implementar logging adequado no `GlobalExceptionHandler`

- **Risco**: Sobrecarga de exceções não tratadas
  - **Mitigação**: Adicionar um handler genérico para `Exception` como último recurso
