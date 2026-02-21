# Pontos de Melhoria - Projeto Clean Architecture

## 1. Violações da Clean Architecture (FEITO)
- **Dependências Inadequadas no Core**: Os casos de uso estão importando `PasswordEncoder` do Spring Security e exceções da camada infrastructure. O core deve depender apenas de interfaces próprias ou do domínio.
  - **Solução**: Criar interface `PasswordEncoderGateway` no core e implementar na infrastructure.
  - **Arquivos afetados**: `CreateUserUseCaseImpl.java`, `UpdateUserUseCaseImpl.java`

- **Exceções no Core**: Uso de `DuplicateResourceException` da infrastructure nos casos de uso.
  - **Solução**: Definir exceções de domínio no core (ex: `DomainException`, `UserAlreadyExistsException`).

## 2. Separação de Responsabilidades (FEITO)
- **Controller Único para Múltiplas Responsabilidades**: `UserController` contém endpoints de autenticação e gerenciamento de usuários.
  - **Solução**: Criar `AuthController` separado para registro e login.

- **Ausência de Caso de Uso de Autenticação**: Não há caso de uso dedicado para autenticação no core.
  - **Solução**: Implementar `AuthenticateUserUseCase` no core.

## 3. Inconsistências Técnicas (FEITO)
- **Versão do Spring Boot**: pom.xml declara versão 4.0.0, que não existe (atual é 3.x).
  - **Solução**: Atualizar para versão estável como 3.2.x.

- **Mapeamento Manual**: Mappers implementados manualmente, gerando boilerplate.
  - **Solução**: Adicionar MapStruct ao projeto para geração automática de mappers.

- **Tratamento de Erros Inconsistente**: Mensagens em português e inglês misturadas, tipos de exceções variados.
  - **Solução**: Padronizar mensagens em inglês e usar hierarquia de exceções consistente.

## 4. Validação e Segurança (PARCIAL)
- **Falta de Validação no Domínio**: Entidade `User` não valida suas próprias regras de negócio.
  - **Solução**: Adicionar métodos de validação na entidade `User`.
  - **Status**: NÃO FEITO - Existe `DomainValidator` externo, mas `User` não tem métodos próprios (Foi adicionado no UseCase)

- **Lógica de Senha Exposta**: Hash de senha no caso de uso em vez de encapsulado.
  - **Solução**: Mover para o gateway de senha.
  - **Status**: FEITO - `PasswordEncoderGateway` implementado e utilizado

## 5. Testes
- **Ausência de Testes**: Projeto não possui testes unitários ou de integração.
  - **Solução**: Adicionar testes para:
    - Casos de uso (unitários com mocks)
    - Entidades (validações)
    - Controllers (integração)
    - Mappers

## 6. Outros Melhoramentos
- **Enums PostgreSQL**: Uso de `@ColumnTransformer` pode causar problemas de portabilidade.
  - **Solução**: Considerar mapeamento nativo ou tipos customizados.
  - **Status**: FEITO - Removido `@ColumnTransformer` e `columnDefinition`, usando apenas `@Enumerated(EnumType.STRING)`

- **Logs Estruturados**: Adicionar mais logs para facilitar debugging e monitoramento.

- **Documentação da API**: Adicionar Swagger/OpenAPI para documentação automática dos endpoints.

- **Configuração de Ambiente**: Melhorar application.properties com perfis (dev, test, prod).

## Priorização das Melhorias
1. **Alta Prioridade**: Corrigir violações da Clean Architecture (dependências do core)
2. **Alta Prioridade**: Separar controllers e adicionar caso de uso de autenticação
3. **Média Prioridade**: Adicionar testes unitários
4. **Média Prioridade**: Implementar MapStruct
5. **Baixa Prioridade**: Melhorar logs e documentação

## Benefícios Esperados
- **Manutenibilidade**: Código mais fácil de modificar e estender
- **Testabilidade**: Maior cobertura de testes e isolamento de dependências
- **Consistência**: Padrões uniformes em todo o projeto
- **Escalabilidade**: Estrutura preparada para crescimento
