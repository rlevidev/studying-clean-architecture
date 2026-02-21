# Authentication System with Clean Architecture in Java

This repository contains a study project that implements an authentication system following the Clean Architecture principles proposed by Robert C. Martin (Uncle Bob). The system includes features such as user registration, login, profile management, and JWT (JSON Web Tokens) based authentication.

## 📖 Documentation

The project uses **SpringDoc OpenAPI** (Swagger) for API documentation. You can explore and test the endpoints directly through the web interface.

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Postman Collection
A Postman collection is available in the `Studying_Clean_Architecture - Collection` directory for import.

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── rlevi/
│   │           └── studying_clean_architecture/
│   │               ├── StudyingCleanArchitectureApplication.java  # Application entry point
│   │               │
│   │               ├── core/                      # Domain and Use Cases Layer
│   │               │   ├── entities/              # Domain entities (User, Token, etc.)
│   │               │   ├── enums/                 # Domain enums (Role, TokenType, etc.)
│   │               │   ├── gateway/               # Ports (interfaces) for external services
│   │               │   └── usecases/              # Use cases (CreateUser, AuthenticateUser, etc.)
│   │               │
│   │               └── infrastructure/            # Concrete implementations and frameworks
│   │                   ├── beans/                 # Spring bean configurations
│   │                   ├── config/                # Security and JWT configurations
│   │                   ├── dto/                   # Data Transfer Objects
│   │                   ├── exception/             # Exception handling
│   │                   ├── gateway/               # Outgoing port implementations
│   │                   ├── mapper/                # Mappers between DTOs and entities
│   │                   ├── persistence/           # Persistence implementations
│   │                   └── presentation/          # REST API controllers and DTOs
│   │
│   └── resources/
│       ├── application.properties       # Application configurations
│       ├── db/                          # Flyway migrations
│       └── logback-spring.xml           # Log configuration
│
└── test/                                # Automated tests
    └── java/
        └── com/rlevi/studying_clean_architecture/
            ├── core/                    # Domain unit tests
            │   └── usecases/            # Use case tests
            └── infrastructure/          # Integration tests
                ├── config/              # Configuration tests
                ├── controllers/         # API tests
                └── security/            # Security tests
```

## 📚 Architecture Layers

### 1. Domain Layer (Core)
**Location**: `core/`

The Core layer is the heart of the application and contains all business rules and domain entities. It is completely independent of frameworks and external libraries.

#### `entities/`
- **Responsibility**: Represent fundamental authentication domain concepts.
- **Examples**: 
  - `User`: Represents a system user with basic data.
  - `Token`: Manages authentication and refresh tokens.
- **Characteristics**:
  - Contain only domain logic.
  - Are POJOs (Plain Old Java Objects).
  - Do not have framework annotations.
  - Are immutable when possible.
  - Validate their own business rules.

#### `enums/`
- **Responsibility**: Defines enumerated types used in the domain.
- **Examples**: 
  - `Role`: User profile types (ADMIN, USER).
  - `TokenType`: Token types (BEARER, REFRESH).
- **Characteristics**:
  - Define domain constant values.
  - Improve readability and maintenance.
  - Avoid "magic strings" in the code.

#### `gateway/`
- **Responsibility**: Defines interfaces (ports) that the domain needs to communicate with the outside world.
- **Examples**:
  - `UserGateway`: User persistence operations.
  - `TokenGateway`: JWT token operations.
  - `PasswordEncoder`: Password encoding and verification.
- **Characteristics**:
  - Are only interfaces (contracts).
  - Follow the Dependency Inversion Principle.
  - Are implemented by the infrastructure layer.
  - Allow testing the domain in isolation.

#### `usecases/`
- **Responsibility**: Implements the application's business rules.
- **Examples**:
  - `createuser/`: Creating new users.
  - `authenticate/`: Authenticating users.
  - `refreshToken/`: Renewing access tokens.
- **Characteristics**:
  - Implement complex business rules.
  - Are independent of frameworks.
  - Have no external dependencies.
  - Receive and return domain objects.
  - Are highly testable.
  - Follow the Single Responsibility Principle.
  - Are stateless.
  - Throw domain-specific exceptions.

### 2. Infrastructure Layer
**Location**: `infrastructure/`

The Infrastructure layer contains concrete implementations that allow the application to interact with the outside world. Unlike the Core layer, it can depend on external frameworks and libraries.

#### `config/`
- **Responsibility**: Application configurations, security, and JWT.
- **Characteristics**:
  - Contains Spring configuration classes.
  - Defines Spring beans and components.
  - Configures technical aspects like security, CORS, etc.

#### `dto/`
- **Responsibility**: Objects for data transfer between layers.
- **Characteristics**:
  - Simple data structures.
  - May contain validation annotations.
  - Optimized for API communication.
  - Can be serialized/deserialized to JSON.

#### `exception/`
- **Responsibility**: Global exception handling.
- **Characteristics**:
  - Captures and handles exceptions thrown by controllers.
  - Converts exceptions into appropriate HTTP responses.

#### `gateway/`
- **Responsibility**: Concrete implementations of ports defined in core.
- **Characteristics**:
  - Implement interfaces defined in core.
  - May depend on external frameworks (e.g., Spring Data JPA).
  - Injected into use cases via dependency injection.

#### `mapper/`
- **Responsibility**: Conversion between entities and DTOs.
- **Characteristics**:
  - Use libraries like MapStruct.
  - Isolate conversion logic.

#### `persistence/`
- **Responsibility**: JPA entities and repositories.
- **Characteristics**:
  - Contain JPA/Hibernate annotations.
  - Map to database tables.

#### `presentation/`
- **Responsibility**: REST controllers and API DTOs.
- **Characteristics**:
  - Contain Spring MVC annotations.
  - Validate API inputs.
  - Convert DTOs to domain objects.

## 🛠️ Technologies Used

- **Java 17** - Main programming language
- **Spring Boot** - Framework for Java applications
- **Spring Security** - Authentication and authorization
- **JWT** - JSON Web Tokens for stateless authentication
- **H2 Database** - In-memory database for development/testing
- **PostgreSQL** - Production-ready database support
- **Flyway** - Database migration
- **Lombok** - Reduction of boilerplate code
- **MapStruct** - Object mapping
- **SpringDoc OpenAPI (Swagger)** - API documentation
- **JUnit 5 & Mockito** - Testing framework

## 🚀 How to Run

### Zero Configuration (H2 In-Memory)
For a quick test, the project is configured to run with H2 by default. You don't need Docker or a local database.

1. Clone the repository:
   ```bash
   git clone https://github.com/your-user/studying_clean_architecture.git
   cd studying_clean_architecture
   ```

2. Run with Maven:
   ```bash
   ./mvnw spring-boot:run
   ```

3. Access the H2 Console:
   - **URL**: `http://localhost:8080/h2-console`
   - **JDBC URL**: `jdbc:h2:mem:clean_arch_db`
   - **User**: `sa` / **Password**: (empty)

### Running with PostgreSQL (Docker)
If you prefer to test with PostgreSQL:

1. Start the database with Docker Compose:
   ```bash
   docker-compose up -d
   ```

2. Run the application with the `postgres` profile:
   ```bash
   ./mvnw spring-boot:run -Dspring.profiles.active=postgres
   ```

## 🧪 Testing
```bash
# Run all tests
./mvnw test
```

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
