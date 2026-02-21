# Comunicação e Fluxo de Dados na Clean Architecture

Este documento explica detalhadamente como funciona a comunicação entre as camadas e componentes da Clean Architecture implementada neste projeto, utilizando os fluxos de **criação de usuário** e **login** como exemplos concretos.

---

## 1. Visão Geral da Arquitetura

A Clean Architecture organiza o projeto em camadas concêntricas, onde cada camada tem uma responsabilidade específica e depende apenas de abstrações das camadas internas:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        CAMADA DE PRESENTATION                            │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                      UserController                              │    │
│  │   (Recebe HTTP Requests, retorna HTTP Responses, delega para    │    │
│  │    Use Cases, converte DTOs para Domain Entities)               │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          CAMADA DE CORE                                  │
│                                                                         │
│  ┌─────────────────────────┐     ┌─────────────────────────────────┐    │
│  │       Use Cases         │     │           Entities              │    │
│  │                         │     │                                 │    │
│  │  • CreateUserUseCase    │     │  • User (record)                │    │
│  │  • LoginUserUseCase     │     │  • Role (enum)                  │    │
│  │  • FindUserByEmail      │     │                                 │    │
│  │  • etc...               │     │                                 │    │
│  └─────────────────────────┘     └─────────────────────────────────┘    │
│             │                                │                          │
│             │         ┌──────────────────────┘                          │
│             │         │                                                  │
│             ▼         ▼                                                  │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                        Gateways (Interfaces)                     │    │
│  │                                                                 │    │
│  │  • UserGateway (interface abstrata)                             │    │
│  │  • PasswordEncoderGateway (interface abstrata)                  │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
                                      │
                    INVERSÃO DE DEPENDÊNCIA
                    (core não conhece infraestrutura)
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                       CAMADA DE INFRAESTRUTURA                          │
│                                                                         │
│  ┌─────────────────────────┐     ┌─────────────────────────────────┐    │
│  │   Gateway Implementations│     │            Beans                │    │
│  │                         │     │                                 │    │
│  │  • UserRepositoryGateway│     │  • BeanConfiguration            │    │
│  │  • BCryptPasswordEncoder│     │    (Injeta dependências)        │    │
│  └─────────────────────────┘     └─────────────────────────────────┘    │
│             │                                                        │
│             ▼                                                        │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                        Repositories                              │    │
│  │                                                                 │    │
│  │  • UserRepository (Spring Data JPA)                             │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                       Mappers e DTOs                             │    │
│  │                                                                 │    │
│  │  • UserMapper (converte entre camadas)                          │    │
│  │  • UserLoginRequest, UserRegisterRequest (DTOs)                 │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           BANCO DE DADOS                                 │
│                                                                         │
│  • PostgreSQL (banco relacional)                                       │
│  • Tabela users (criada via Flyway migrations)                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Inversão de Dependência

Este é um dos conceitos mais importantes da Clean Architecture. Observe que:

1. **Camada Core** (Use Cases, Entities) **NÃO conhece** a camada de Infraestrutura
2. **Camada Core** define **interfaces abstratas** (Gateways)
3. **Camada Infraestrutura** **implementa** essas interfaces

```
         CORE (Define Interfaces)              INFRAESTRUTURA (Implementa)
              
              UserGateway                             UserRepositoryGateway
                    ▲                                        │
                    │                                        │
                    │          implements                    │
                    └────────────────────────────────────────┘

         O UseCase conhece                         O Gateway Impl conhece
         apenas a interface                        a implementação concreta
```

**Exemplo no código:**

```java
// ========== CAMADA CORE ==========
// UserGateway.java (interface abstrata)
public interface UserGateway {
  User createUser(User user);
  Optional<User> findUserByEmail(String email);
  // ... outros métodos
}

// ========== CAMADA INFRAESTRUTURA ==========
// UserRepositoryGateway.java (implementação concreta)
@Component
public class UserRepositoryGateway implements UserGateway {
  
  private final UserRepository userRepository;  // Conhece JPA
  private final UserMapper userMapper;           // Conhece mapeamento
  
  @Override
  public User createUser(User user) {
    // Implementação real de acesso ao banco
    UserEntity entity = userMapper.toEntity(user);
    UserEntity saved = userRepository.save(entity);
    return userMapper.toDomain(saved);
  }
}
```

---

## 3. Fluxo Completo: Criação de Usuário

Vamos seguir o fluxo de uma requisição HTTP para criar um novo usuário:

```
HTTP POST /api/v1/users/register
{
  "email": "usuario@email.com",
  "name": "Nome do Usuário",
  "password": "senha123"
}
```

### 3.1 Controller Recebe a Requisição HTTP

```java
// UserController.java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
  
  private final CreateUserUseCase createUserUseCase;
  private final UserMapper userMapper;
  private final JwtUtil jwtUtil;
  
  // Injeção via construtor (Spring gerencia)
  public UserController(CreateUserUseCase createUserUseCase, 
                       UserMapper userMapper, 
                       JwtUtil jwtUtil) {
    this.createUserUseCase = createUserUseCase;
    this.userMapper = userMapper;
    this.jwtUtil = jwtUtil;
  }
  
  @PostMapping("/register")
  public ResponseEntity<UserRegisterResponse> registerUser(
      @Valid @RequestBody UserRegisterRequest request) {
    
    // Passo 1: Converter DTO → Domain Entity
    User userToCreate = userMapper.toDomain(request);
    
    // Passo 2: Delegar para o Use Case
    createUserUseCase.execute(userToCreate);
    
    // Passo 3: Gerar token JWT
    String token = jwtUtil.generateToken(request.email(), "ROLE_USER");
    
    // Passo 4: Retornar resposta
    return ResponseEntity.ok(UserRegisterResponse.success(token));
  }
}
```

### 3.2 Conversão de DTO para Domain Entity

```java
// UserMapper.java
@Component
public class UserMapper {
  
  // Converte UserRegisterRequest (DTO) → User (Domain Entity)
  public User toDomain(UserRegisterRequest dto) {
    return new User(
      null,                    // id: ainda não existe
      dto.email(),
      dto.name(),
      dto.password(),          // senha será criptografada no Use Case
      null,                    // role: será definido no Use Case
      null,                    // createdAt: gerado pelo banco
      null                     // updatedAt: gerado pelo banco
    );
  }
}
```

### 3.3 Use Case Executa a Lógica de Negócio

```java
// CreateUserUseCaseImpl.java
public class CreateUserUseCaseImpl implements CreateUserUseCase {
  
  private final UserGateway userGateway;
  private final PasswordEncoderGateway passwordEncoderGateway;
  
  // Injeção via construtor
  public CreateUserUseCaseImpl(UserGateway userGateway, 
                              PasswordEncoderGateway passwordEncoderGateway) {
    this.userGateway = userGateway;
    this.passwordEncoderGateway = passwordEncoderGateway;
  }
  
  @Override
  public User execute(User user) {
    // Passo 1: Validar se email já existe
    if (userGateway.verifyExistsByEmail(user.email())) {
      throw new DuplicateResourceException(
        "The email provided is already in use. Please use another email or log in.");
    }
    
    // Passo 2: Criptografar a senha
    String encryptedPassword = passwordEncoderGateway.encode(user.passwordHash());
    
    // Passo 3: Criar novo objeto com dados processados
    var userToSave = new User(
      null,                    // id será gerado pelo banco
      user.email(),
      user.name(),
      encryptedPassword,       // senha criptografada
      Role.USER,               // papel padrão
      null,                    // datas serão geradas pelo banco
      null
    );
    
    // Passo 4: Delegar para o Gateway (salvar no banco)
    return userGateway.createUser(userToSave);
  }
}
```

### 3.4 Gateway Implementa Acesso a Dados

```java
// UserRepositoryGateway.java
@Component
public class UserRepositoryGateway implements UserGateway {
  
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  
  public UserRepositoryGateway(UserRepository userRepository, 
                              UserMapper userMapper) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
  }
  
  @Override
  public User createUser(User user) {
    // Converter Domain → Entity (banco)
    UserEntity userEntity = userMapper.toEntity(user);
    
    // Salvar usando Spring Data JPA
    UserEntity savedEntity = userRepository.save(userEntity);
    
    // Converter Entity → Domain (retornar)
    return userMapper.toDomain(savedEntity);
  }
}
```

### 3.5 Mapper Converte Entre Camadas

```java
// UserMapper.java
@Component
public class UserMapper {
  
  // Domain → Entity (antes de salvar no banco)
  public UserEntity toEntity(User user) {
    UserEntity entity = new UserEntity();
    entity.setName(user.name());
    entity.setEmail(user.email());
    entity.setPasswordHash(user.passwordHash());
    entity.setRole(user.role());
    return entity;
  }
  
  // Entity → Domain (após recuperar do banco)
  public User toDomain(UserEntity entity) {
    return new User(
      entity.getId(),
      entity.getEmail(),
      entity.getName(),
      entity.getPasswordHash(),
      entity.getRole(),
      entity.getCreatedAt(),
      entity.getUpdatedAt()
    );
  }
}
```

### 3.6 Repository Acessa o Banco de Dados

```java
// UserRepository.java (Spring Data JPA)
public interface UserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByEmail(String email);
  boolean existsByEmail(String email);
}
```

---

## 4. Fluxo Completo: Login de Usuário

Agora vamos seguir o fluxo de login:

```
HTTP POST /api/v1/users/login
{
  "email": "usuario@email.com",
  "password": "senha123"
}
```

### 4.1 Controller Recebe a Requisição

```java
// UserController.java
@PostMapping("/login")
public ResponseEntity<UserLoginResponse> loginUser(
    @Valid @RequestBody UserLoginRequest request) {
  
  // Converter DTO → Domain Entity
  User userToLogin = userMapper.toDomain(request);
  
  // Delegar para o Use Case
  User authenticatedUser = loginUserUseCase.execute(userToLogin);
  
  // Extrair role do usuário autenticado
  String role = authenticatedUser.role().name();
  
  // Gerar token JWT
  String token = jwtUtil.generateToken(request.email(), role);
  
  return ResponseEntity.ok(UserLoginResponse.success(token));
}
```

### 4.2 Use Case Valida Credenciais

```java
// LoginUserUseCaseImpl.java
public class LoginUserUseCaseImpl implements LoginUserUseCase {
  
  private final UserGateway userGateway;
  private final PasswordEncoderGateway passwordEncoderGateway;
  
  public LoginUserUseCaseImpl(UserGateway userGateway, 
                              PasswordEncoderGateway passwordEncoderGateway) {
    this.userGateway = userGateway;
    this.passwordEncoderGateway = passwordEncoderGateway;
  }
  
  @Override
  public User execute(User user) {
    // Passo 1: Validar entrada
    if (user == null || user.email() == null || user.passwordHash() == null) {
      throw new AuthenticationException("Invalid email or password.");
    }
    
    // Passo 2: Buscar usuário por email
    User foundUser = userGateway.findUserByEmail(user.email())
      .orElseThrow(() -> new AuthenticationException("Invalid email or password."));
    
    // Passo 3: Verificar senha (BCrypt comparison)
    boolean passwordMatches = passwordEncoderGateway.matches(
      user.passwordHash(),
      foundUser.passwordHash()
    );
    
    if (!passwordMatches) {
      throw new AuthenticationException("Invalid email or password.");
    }
    
    // Passo 4: Retornar usuário autenticado
    return foundUser;
  }
}
```

---

## 5. Injeção de Dependências via Beans

O Spring gerencia todas as dependências através dos beans configurados:

```java
// BeanConfiguration.java
@Configuration
public class BeanConfiguration {
  
  // Bean para UserRepositoryGateway
  // Spring automaticamente injeta UserRepository e UserMapper
  @Bean
  public UserGateway userGateway(UserRepository userRepository, 
                                UserMapper userMapper) {
    return new UserRepositoryGateway(userRepository, userMapper);
  }
  
  // Bean para PasswordEncoderGateway
  @Bean
  public PasswordEncoderGateway passwordEncoderGateway() {
    return new BCryptPasswordEncoderGateway();
  }
  
  // Bean para CreateUserUseCase
  // Spring injeta automaticamente as dependências configuradas acima
  @Bean
  public CreateUserUseCase createUserUseCase(UserGateway userGateway, 
                                            PasswordEncoderGateway passwordEncoderGateway) {
    return new CreateUserUseCaseImpl(userGateway, passwordEncoderGateway);
  }
  
  // Bean para LoginUserUseCase
  @Bean
  public LoginUserUseCase loginUserUseCase(UserGateway userGateway,
                                          PasswordEncoderGateway passwordEncoderGateway) {
    return new LoginUserUseCaseImpl(userGateway, passwordEncoderGateway);
  }
}
```

---

## 6. Diagrama de Sequência: Fluxo de Criação de Usuário

```
┌────────┐     ┌──────────────┐     ┌─────────────────┐     ┌──────────────────┐     ┌─────────────┐     ┌─────────┐
│ Client │     │ UserController│     │ CreateUserUseCase│     │ UserGateway      │     │ UserMapper   │     │  Banco  │
└────┬───┘     └──────┬───────┘     └───────┬─────────┘     └────────┬─────────┘     └──────┬──────┘     └────┬────┘
     │                │                      │                        │                     │              │
     │ POST /register │                      │                        │                     │              │
     │────────────────>│                      │                        │                     │              │
     │                │                      │                        │                     │              │
     │                │ userMapper.toDomain()│                        │                     │              │
     │                │──────────────────────>│                        │                     │              │
     │                │                      │                        │                     │              │
     │                │ createUserUseCase.execute(user)              │                     │              │
     │                │──────────────────────>│                        │                     │              │
     │                │                      │                        │                     │              │
     │                │                      │ userGateway.verifyExistsByEmail()            │              │
     │                │                      │──────────────────────────────────────────────>│              │
     │                │                      │                        │                     │              │
     │                │                      │         (retorna: false)│<────────────────────────────────────│
     │                │                      │                        │                     │              │
     │                │                      │ passwordEncoder.encode()│                     │              │
     │                │                      │───────────────────────────────────────────────────────────────>>
     │                │                      │                        │                     │              │
     │                │                      │ userGateway.createUser()│                     │              │
     │                │                      │──────────────────────────────────────────────>│              │
     │                │                      │                        │                     │              │
     │                │                      │                        │ userMapper.toEntity()              │
     │                │                      │                        │───────────────────────────────────>>
     │                │                      │                        │                     │              │
     │                │                      │                        │ userRepository.save()              │
     │                │                      │                        │───────────────────────────────────────────>>
     │                │                      │                        │                     │              │ INSERT
     │                │                      │                        │                     │              │──────>
     │                │                      │                        │                     │              │
     │                │                      │                        │         (retorna entity com id)│<───────
     │                │                      │                        │                     │              │
     │                │                      │                        │ userMapper.toDomain()              │
     │                │                      │                        │───────────────────────────────────>>
     │                │                      │                        │                     │              │
     │                │                      │                        │         (retorna User)│<─────────────
     │                │                      │                        │                     │              │
     │                │         (retorna User)│<────────────────────────────────────────────│              │
     │                │                      │                        │                     │              │
     │                │ jwtUtil.generateToken()│                      │                     │              │
     │                │───────────────────────────────────────────────────────────────────────────────────────────>>
     │                │                      │                        │                     │              │
     │<────────────────│ 201 Created + JWT   │                      │                     │              │
     │                │                      │                        │                     │              │
```

---

## 7. Resumo das Responsabilidades por Camada

| Camada | Componente | Responsabilidade |
|--------|------------|------------------|
| **Presentation** | UserController | Receber HTTP, delegar para Use Cases, retornar HTTP Responses |
| **Core - Use Cases** | CreateUserUseCase, LoginUserUseCase | Contém lógica de negócio, validações, orquestração |
| **Core - Entities** | User, Role | Define estrutura de dados e regras de negócio centrais |
| **Core - Gateways** | UserGateway, PasswordEncoderGateway | Define contratos/abstrações (interfaces) |
| **Infra - Gateways Impl** | UserRepositoryGateway, BCryptPasswordEncoderGateway | Implementa acesso a dados e serviços externos |
| **Infra - Beans** | BeanConfiguration | Configura injeção de dependências do Spring |
| **Infra - Repositories** | UserRepository | Acesso ao banco via Spring Data JPA |
| **Infra - Mappers** | UserMapper | Converte dados entre camadas (DTO ↔ Entity ↔ Domain) |

---

## 8. Vantagens desta Arquitetura

1. **Testabilidade**: Use Cases podem ser testados sem banco de dados (mockando Gateways)
2. **Flexibilidade**: Pode trocar a implementação de infraestrutura sem alterar o core
3. **Manutenibilidade**: Cada camada tem responsabilidade única e bem definida
4. **Desacoplamento**: Core não depende de frameworks ou banco de dados específicos
5. **Reusabilidade**: Use Cases podem ser reutilizados em diferentes contextos (REST, GraphQL, CLI)

---

## 9. Referências dos Arquivos do Projeto

| Arquivo | Caminho | Propósito |
|---------|---------|-----------|
| UserController | `infrastructure/presentation/` | Controlador HTTP |
| CreateUserUseCase | `core/usecases/createuser/` | Interface do Use Case |
| CreateUserUseCaseImpl | `core/usecases/createuser/` | Implementação do Use Case |
| LoginUserUseCaseImpl | `core/usecases/loginuser/` | Implementação do Use Case |
| User | `core/entities/` | Entidade de domínio |
| UserGateway | `core/gateway/` | Interface do Gateway |
| UserRepositoryGateway | `infrastructure/gateway/` | Implementação do Gateway |
| UserMapper | `infrastructure/mapper/` | Conversão de dados |
| UserRepository | `infrastructure/persistence/` | Repositório JPA |
| BeanConfiguration | `infrastructure/beans/` | Configuração de DI |
| UserRegisterRequest | `infrastructure/dto/register/` | DTO de registro |
| UserLoginRequest | `infrastructure/dto/login/` | DTO de login |
