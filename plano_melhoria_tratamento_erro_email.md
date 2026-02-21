# Plano para Melhorar o Tratamento de Erro de E-mail Já Cadastrado

## Objetivo
Melhorar a experiência do usuário ao tentar cadastrar um e-mail que já existe no sistema, retornando uma mensagem de erro clara e um código de status HTTP apropriado (409 Conflict).

## Passos para Implementação

1. **Modificar o `CreateUserUseCaseImpl`**
   - Alterar o lançamento de `IllegalArgumentException` para `DuplicateResourceException`
   - Garantir que a mensagem de erro seja clara e amigável

2. **Verificar a existência do `DuplicateResourceException`**
   - Já existe uma classe `DuplicateResourceException` no projeto
   - Já existe um manipulador para `DuplicateResourceException` no `GlobalExceptionHandler`

3. **Testar a alteração**
   - Verificar se o endpoint retorna o status 409 Conflict
   - Confirmar que a mensagem de erro é clara e informativa

## Código a ser modificado

### 1. CreateUserUseCaseImpl.java
```java
// Alterar de:
if (userGateway.verifyExistsByEmail(user.email())) {
  throw new IllegalArgumentException("Email já está cadastrado.");
}

// Para:
if (userGateway.verifyExistsByEmail(user.email())) {
  throw new DuplicateResourceException("O e-mail informado já está em uso. Por favor, utilize outro e-mail ou faça login.");
}
```

## Benefícios
- Melhor experiência do usuário com mensagens de erro claras
- Código de status HTTP apropriado (409 Conflict) para recursos duplicados
- Consistência no tratamento de erros na aplicação

## Testes Recomendados
1. Tentar cadastrar um usuário com e-mail já existente
2. Verificar se o status da resposta é 409 Conflict
3. Confirmar que a mensagem de erro é clara e informativa
4. Verificar os logs para garantir que não há erros inesperados
