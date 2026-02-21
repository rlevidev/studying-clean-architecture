# Architecture - Sistema de Autenticação

## Estrutura de Diretórios

```
src/main/java/com/rlevi/studying_clean_architecture/
├── core/                          # Camada de domínio
│   ├── entities/                  # Entidades (User.java)
│   ├── enums/                     # Enums (Role.java)
│   ├── exception/                 # Exceções de domínio
│   ├── gateway/                   # Interfaces (portas)
│   ├── usecases/                  # Casos de uso
│   └── utils/                     # Utilitários
└── infrastructure/                # Implementações concretas
    ├── beans/                     # Configuração Spring
    ├── dto/                       # Data Transfer Objects
    ├── exception/                 # Tratamento de exceções
    ├── gateway/                   # Implementações de gateways
    ├── mapper/                    # Mapeadores DTO-Entidade
    ├── persistence/               # JPA Entities e Repositories
    ├── presentation/              # Controllers REST
    └── security/                  # JWT e configuração de segurança
```

## Padrões Utilizados
- **Clean Architecture**: Separação core/infrastructure
- **Repository Pattern**: UserRepositoryGateway
- **Dependency Injection**: Spring beans
- **DTO Pattern**: Separação API-Domínio
- **Strategy**: PasswordEncoderGateway

## Decisões Técnicas
- Spring Boot 3.2.0 com Java 17
- PostgreSQL com Flyway migrations
- JWT para autenticação stateless
- BCrypt para hash de senhas
- Bean Validation para validação de entrada

## Fontes de Verdade
- Endpoints: [UserController.java](src/main/java/com/rlevi/studying_clean_architecture/infrastructure/presentation/UserController.java)
- Entidade User: [User.java](src/main/java/com/rlevi/studying_clean_architecture/core/entities/User.java)
- Configuração JWT: [SecurityConfig.java](src/main/java/com/rlevi/studying_clean_architecture/infrastructure/security/SecurityConfig.java)
- Fluxos completos: [docs/CLEAN_ARCHITECTURE_FLUXO_COMPLETO.md](docs/CLEAN_ARCHITECTURE_FLUXO_COMPLETO.md)
