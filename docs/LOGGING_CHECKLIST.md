# Checklist de Implementação de Logs

## Visão Geral

Este checklist fornece um guia passo-a-passo para implementar o sistema de logs no projeto de autenticação Clean Architecture.

## Pré-requisitos

- [ ] Entender a estrutura do projeto (Clean Architecture)
- [ ] Conhecer os pontos críticos onde os logs são necessários
- [ ] Ter acesso ao código fonte do projeto

## Etapas de Implementação

### 1. Configuração Inicial

#### 1.1 Adicionar dependências ao pom.xml
- [ ] Adicionar SLF4J API
- [ ] Adicionar Logback Classic
- [ ] Adicionar Logstash Logback Encoder (para logs JSON)
- [ ] Verificar se Spring Boot Starter Logging já está presente

#### 1.2 Criar arquivos de configuração
- [ ] Criar `src/main/resources/application.properties` com configuração de logging
- [ ] Criar `src/main/resources/logback-spring.xml` para configuração avançada
- [ ] Configurar níveis de log para diferentes ambientes (dev/prod)

### 2. Implementação do Utilitário de Logs

#### 2.1 Criar LoggerUtils
- [ ] Criar `src/main/java/com/rlevi/studying_clean_architecture/core/utils/LoggerUtils.java`
- [ ] Implementar métodos básicos (startRequest, logSuccess, logError, etc.)
- [ ] Implementar métodos específicos (logValidation, logAccess, logExecutionTime)
- [ ] Testar o utilitário com exemplos simples

### 3. Implementação nos Controllers

#### 3.1 UserController
- [ ] Adicionar logger estático no início da classe
- [ ] Implementar logging no método `registerUser`
  - [ ] Log de início de requisição
  - [ ] Log de validação de entrada
  - [ ] Log de sucesso/erro
  - [ ] Log de finalização de requisição
- [ ] Implementar logging no método `loginUser`
  - [ ] Log de tentativa de login
  - [ ] Log de autenticação bem-sucedida
  - [ ] Log de falha de autenticação
- [ ] Implementar logging nos métodos de administração
  - [ ] Log de acesso a recursos protegidos
  - [ ] Log de operações CRUD

### 4. Implementação nos Use Cases

#### 4.1 CreateUserUseCase
- [ ] Adicionar logger estático
- [ ] Implementar logging nas validações de domínio
- [ ] Implementar logging na verificação de existência de email
- [ ] Implementar logging na criptografia de senha
- [ ] Implementar logging no salvamento no banco
- [ ] Implementar logging de tempo de execução

#### 4.2 LoginUserUseCase
- [ ] Adicionar logger estático
- [ ] Implementar logging na validação de credenciais
- [ ] Implementar logging na busca de usuário
- [ ] Implementar logging na validação de senha
- [ ] Implementar logging de autenticação bem-sucedida

#### 4.3 Outros Use Cases
- [ ] FindAllUsersUseCase
- [ ] FindUserByIdUseCase
- [ ] FindUserByEmailUseCase
- [ ] VerifyExistsByEmailUseCase
- [ ] DeleteUserUseCase
- [ ] UpdateUserUseCase

### 5. Implementação nos Gateways

#### 5.1 UserGateway
- [ ] Adicionar logger estático
- [ ] Implementar logging nas operações de banco de dados
- [ ] Implementar logging de consultas
- [ ] Implementar logging de erros de banco de dados

#### 5.2 PasswordEncoderGateway
- [ ] Adicionar logger estático
- [ ] Implementar logging nas operações de criptografia
- [ ] Implementar logging de validação de senhas

### 6. Implementação nos Repositórios

#### 6.1 UserRepository
- [ ] Adicionar logger estático
- [ ] Implementar logging nas consultas JPA
- [ ] Implementar logging de operações de save/update/delete
- [ ] Implementar logging de exceções de banco de dados

### 7. Implementação nos Handlers de Exceção

#### 7.1 GlobalExceptionHandler
- [ ] Adicionar logger estático
- [ ] Implementar logging para ResourceNotFoundException
- [ ] Implementar logging para DuplicateResourceException
- [ ] Implementar logging para BusinessException
- [ ] Implementar logging para AuthenticationException
- [ ] Implementar logging para AccessDeniedException
- [ ] Implementar logging para MethodArgumentNotValidException
- [ ] Implementar logging para Exception genérica

### 8. Configuração de Ambientes

#### 8.1 Desenvolvimento
- [ ] Configurar nível DEBUG para camada core
- [ ] Configurar nível INFO para camada infrastructure
- [ ] Habilitar logs detalhados de validação
- [ ] Configurar console logging

#### 8.2 Produção
- [ ] Configurar nível INFO para operações críticas
- [ ] Configurar nível WARN para problemas não críticos
- [ ] Configurar nível ERROR para falhas
- [ ] Configurar file logging com rotação
- [ ] Desativar logs DEBUG

### 9. Testes e Validação

#### 9.1 Testes Unitários
- [ ] Testar LoggerUtils com diferentes cenários
- [ ] Testar logging nos use cases
- [ ] Testar logging nos controllers
- [ ] Testar logging nos handlers de exceção

#### 9.2 Testes de Integração
- [ ] Testar fluxo completo de registro de usuário
- [ ] Testar fluxo completo de login
- [ ] Testar fluxo de administração de usuários
- [ ] Testar tratamento de exceções

#### 9.3 Validação de Performance
- [ ] Medir impacto de performance dos logs
- [ ] Verificar consumo de memória
- [ ] Testar com carga de requisições

### 10. Documentação e Treinamento

#### 10.1 Documentação
- [ ] Atualizar README com informações sobre logging
- [ ] Criar guia de boas práticas de logging
- [ ] Documentar formatos de logs
- [ ] Documentar consultas comuns de logs

#### 10.2 Treinamento
- [ ] Treinar equipe sobre uso do LoggerUtils
- [ ] Treinar equipe sobre análise de logs
- [ ] Treinar equipe sobre configuração de níveis de log

## Verificação Final

### 11. Checklist de Qualidade

#### 11.1 Consistência
- [ ] Todos os métodos críticos têm logging
- [ ] Mensagens de log são consistentes
- [ ] Uso de MDC é consistente
- [ ] Níveis de log são apropriados

#### 11.2 Segurança
- [ ] Nenhuma informação sensível é logada
- [ ] Senhas não são incluídas nos logs
- [ ] Tokens JWT não são logados completos
- [ ] Dados pessoais são tratados com cuidado

#### 11.3 Performance
- [ ] Logs não impactam significativamente a performance
- [ ] Uso de lazy evaluation para mensagens complexas
- [ ] Níveis de log apropriados para produção
- [ ] Rotação de arquivos configurada

#### 11.4 Monitoramento
- [ ] Logs estruturados para análise automatizada
- [ ] Identificadores de rastreamento presentes
- [ ] Métricas de performance registradas
- [ ] Erros bem documentados

## Comandos Úteis

### Verificar dependências
```bash
./mvnw dependency:tree | grep logback
```

### Testar configuração de logs
```bash
./mvnw spring-boot:run -Dlogging.level.com.rlevi.studying_clean_architecture=DEBUG
```

### Consultar logs gerados
```bash
# Logs de erro
tail -f logs/application.log | grep ERROR

# Logs de uma requisição específica
grep "550e8400-e29b-41d4-a716-446655440000" logs/application.log

# Tempo de execução de operações
grep "Operation completed.*ms" logs/application.log
```

## Troubleshooting

### Problemas Comuns

#### 1. Logs não aparecem
- [ ] Verificar nível de log configurado
- [ ] Verificar configuração do logback
- [ ] Verificar se o logger está sendo instanciado corretamente

#### 2. Performance lenta
- [ ] Verificar se logs DEBUG estão habilitados em produção
- [ ] Verificar uso de mensagens complexas sem lazy evaluation
- [ ] Verificar tamanho dos arquivos de log

#### 3. Falta de contexto
- [ ] Verificar uso do MDC
- [ ] Verificar inclusão de informações relevantes
- [ ] Verificar consistência das mensagens

## Próximos Passos

### Implementação Gradual
1. **Fase 1**: Implementar LoggerUtils e configuração básica
2. **Fase 2**: Implementar logging nos controllers
3. **Fase 3**: Implementar logging nos use cases críticos
4. **Fase 4**: Implementar logging nos gateways e repositórios
5. **Fase 5**: Implementar logging nos handlers de exceção
6. **Fase 6**: Testes, validação e documentação

### Integração com Ferramentas
- [ ] Configurar ELK Stack para análise de logs
- [ ] Configurar Prometheus + Grafana para métricas
- [ ] Configurar Sentry para monitoramento de exceções
- [ ] Configurar alertas baseados em logs

## Conclusão

Este checklist fornece um caminho claro para implementar um sistema de logs robusto e eficiente no projeto. A implementação gradual permite validar cada etapa e garantir a qualidade do sistema de logging.