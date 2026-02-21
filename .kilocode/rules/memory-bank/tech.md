# Tech - Sistema de Autenticação

## Tecnologias
- Java 17 (LTS)
- Spring Boot 3.2.0
- Spring Security 6.x
- Spring Data JPA
- PostgreSQL 15
- Flyway 10.x
- JWT (jjwt 0.12.3)
- Lombok 1.18.30
- Maven 3.8+

## Dependências Principais
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-validation
- spring-boot-starter-security
- flyway-core + flyway-database-postgresql
- postgresql (runtime)
- lombok (annotation processor)
- jjwt-api/impl/jackson (0.12.3)

## Comandos de Build
```bash
# Build
./mvnw clean package -DskipTests

# Executar testes
./mvnw test

# Executar aplicação
./mvnw spring-boot:run
```

## Variáveis de Ambiente
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/clean_arch_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# JWT
jwt.secret=your-256-bit-secret-key-here-change-this-in-production
jwt.expiration=86400000

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

## Docker
```bash
# Subir PostgreSQL
docker-compose up -d

# Build e executar imagem
docker build -t clean-arch-app .
docker run -p 8080:8080 clean-arch-app
```

## Banco de Dados
- PostgreSQL 15 (container Docker)
- Tabela: users (via Flyway migration V1__create_users_table.sql)
- Conexão: localhost:5432

## Porta da Aplicação
- HTTP: 8080
- CORS habilitado para: http://localhost:3000

## Limitações Conhecidas
- Apenas access token (sem refresh token rotation)
- Sem verificação de email
- Sem rate limiting
- Sem cache implementado
