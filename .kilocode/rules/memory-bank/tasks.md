# Tasks - Tarefas Repetitivas

## Adicionar Novo Caso de Uso

**Diretório:** `core/usecases/{feature}/`

**Passos:**
1. Criar interface em `core/usecases/{feature}/NomeUseCase.java`
2. Implementar em `core/usecases/{feature}/NomeUseCaseImpl.java`
3. Registrar bean em `infrastructure/beans/BeanConfiguration.java`
4. Criar endpoint em `infrastructure/presentation/UserController.java` (se necessário)

**Exceções comuns:** Lançar `DomainException` para erros de negócio

## Adicionar Nova Entidade

**Diretórios:**
- Entidade: `core/entities/`
- JPA Entity: `infrastructure/persistence/`
- Gateway: `core/gateway/`
- Repository: `infrastructure/gateway/`

**Passos:**
1. Criar record em `core/entities/Nome.java`
2. Criar JPA entity em `infrastructure/persistence/NomeEntity.java`
3. Definir interface gateway em `core/gateway/NomeGateway.java`
4. Implementar repository em `infrastructure/gateway/NomeRepositoryGateway.java`
5. Criar mapper em `infrastructure/mapper/NomeMapper.java`
6. Criar DTOs em `infrastructure/dto/`

## Validar Tamanho do Memory Bank

```bash
wc -l .kilocode/rules/memory-bank/*.md
```

Se algum arquivo ultrapassar o limite de alerta, comprima antes de adicionar mais conteúdo.

## Executar Testes

```bash
./mvnw test
```

## Build e Executar

```bash
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

## Docker - Subir PostgreSQL

```bash
docker-compose up -d
```

## Docker - Build e Run da Aplicação

```bash
docker build -t clean-arch-app .
docker run -p 8080:8080 clean-arch-app
```
