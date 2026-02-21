# Plano para Remoção de Dados Sensíveis nas Respostas da API

## Objetivo
Remover informações sensíveis (como hashes de senha) das respostas da API, garantindo que esses dados não sejam expostos em nenhum endpoint.

## Passos Detalhados

1. **Criar DTOs de Resposta Seguros**
   - Criar um `UserResponse` DTO que não inclui o campo `passwordHash`
   - Atualizar o `UserMapper` para converter entre `User` e `UserResponse`

2. **Atualizar o Controlador**
   - Modificar o `UserController` para retornar `UserResponse` em vez de `User`
   - Garantir que todos os endpoints que retornam dados do usuário usem o DTO seguro

3. **Atualizar os Casos de Uso**
   - Modificar os casos de uso para trabalharem com o DTO seguro quando necessário
   - Garantir que o hash da senha nunca seja retornado

4. **Testes**
   - Atualizar os testes existentes para trabalharem com o novo DTO
   - Adicionar testes para garantir que informações sensíveis não vazem

## Recursos Necessários
- Acesso ao código fonte do projeto
- Ambiente de desenvolvimento configurado
- Acesso ao banco de dados para testes

## Critérios de Sucesso
- Nenhum endpoint da API retorna o campo `passwordHash`
- Todas as funcionalidades existentes continuam funcionando
- Os testes passam com sucesso
- O código mantém os princípios do Clean Architecture

## Riscos e Mitigação
1. **Quebra de Funcionalidade**
   - Mitigação: Testes abrangentes antes de enviar para produção

2. **Performance**
   - Mitigação: O uso de DTOs adiciona uma camada de mapeamento, mas o impacto é mínimo

3. **Esquecimento de Endpoints**
   - Mitigação: Revisar todos os endpoints que retornam dados de usuário
