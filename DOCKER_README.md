# Docker Configuration - Clean Architecture Project

Esta configuração Docker permite executar o projeto de autenticação com Clean Architecture em containers isolados.

## Pré-requisitos

### Para Docker
```bash
# Verificar instalação
docker --version
docker-compose --version
```

### Para Podman
```bash
# Verificar instalação
podman --version
podman-compose --version

# Se necessário, instalar podman-compose
sudo apt install podman-compose  # Ubuntu/Debian
# ou
sudo dnf install podman-compose  # Fedora
```

## Estrutura

- **clean_arch_db**: Container com PostgreSQL 15
- **clean_arch**: Container com a aplicação Spring Boot

## Como Utilizar

### 1. Iniciar os Containers

**Com Docker:**
```bash
docker-compose up -d
```

**Com Podman:**
```bash
podman-compose up -d
```

### 2. Verificar os Logs

**Docker:**
```bash
docker-compose logs -f
```

**Podman:**
```bash
podman-compose logs -f
```

### 3. Acessar a Aplicação

A aplicação estará disponível em:
- **API**: http://localhost:8080
- **Banco de Dados**: localhost:5432

### 4. Parar os Containers

**Docker:**
```bash
docker-compose down
```

**Podman:**
```bash
podman-compose down
```

### 5. Parar e Remover Volumes

**Docker:**
```bash
docker-compose down -v
```

**Podman:**
```bash
podman-compose down -v
```

## Variáveis de Ambiente

### Banco de Dados (clean_arch_db)
- `POSTGRES_USER`: postgres
- `POSTGRES_PASSWORD`: postgres
- `POSTGRES_DB`: clean_arch_db

### Aplicação (clean_arch)
- `SPRING_DATASOURCE_URL`: jdbc:postgresql://clean_arch_db:5432/clean_arch_db
- `SPRING_DATASOURCE_USERNAME`: postgres
- `SPRING_DATASOURCE_PASSWORD`: postgres
- `JWT_SECRET`: your-256-bit-secret-key-here-change-this-in-production
- `JWT_EXPIRATION`: 86400000

## Portas

- **8080**: Aplicação Spring Boot
- **5432**: PostgreSQL (acessível apenas internamente pela aplicação)

## Rede

Ambos os containers estão na rede `clean_arch_network` para comunicação interna.

## Volume

O volume `postgres_data` persiste os dados do banco de dados entre as execuções.

## Verificação Rápida

Após iniciar os containers, aguarde aproximadamente 30-60 segundos e teste:

```bash
# Testar registro de usuário
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test@123","name":"Test User"}'

# Testar login
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test@123"}'
```

## Solução de Problemas

### Erro: "short-name did not resolve" (Podman)
O Dockerfile foi atualizado para usar nomes completos de imagens:
- `docker.io/library/maven:3.8.4-openjdk-17`
- `docker.io/library/eclipse-temurin:17-jre-alpine`

### Container não inicia
**Docker:**
```bash
docker-compose ps
docker-compose logs clean_arch
```

**Podman:**
```bash
podman-compose ps
podman-compose logs clean_arch
```

### Conexão com banco falha
**Docker:**
```bash
docker exec -it clean_arch_db psql -U postgres -d clean_arch_db -c "SELECT 1;"
docker-compose restart clean_arch_db
```

**Podman:**
```bash
podman exec -it clean_arch_db psql -U postgres -d clean_arch_db -c "SELECT 1;"
podman-compose restart clean_arch_db
```

### Aplicação não consegue conectar ao banco
- Aguarde 30-60 segundos para o banco inicializar completamente
- Verifique healthcheck: `podman-compose ps` ou `docker-compose ps`
- Verifique logs: `podman-compose logs clean_arch_db` ou `docker-compose logs clean_arch_db`

## Comandos Úteis

### Docker
```bash
# Acessar terminal do banco de dados
docker exec -it clean_arch_db psql -U postgres -d clean_arch_db

# Acessar terminal da aplicação
docker exec -it clean_arch sh

# Reiniciar containers
docker-compose restart

# Verificar uso de recursos
docker stats
```

### Podman
```bash
# Acessar terminal do banco de dados
podman exec -it clean_arch_db psql -U postgres -d clean_arch_db

# Acessar terminal da aplicação
podman exec -it clean_arch sh

# Reiniciar containers
podman-compose restart

# Verificar uso de recursos
podman stats
```

## Notas Importantes

- **Healthcheck**: O banco de dados tem healthcheck para garantir que a aplicação só inicia após o banco estar pronto
- **Restart Policy**: A aplicação reinicia automaticamente se falhar (`unless-stopped`)
- **Flyway**: Executará as migrações automaticamente ao iniciar a aplicação
- **Persistência**: Os dados do banco persistem em um volume Docker/Podman
- **Rede Segura**: Comunicação interna entre containers via rede bridge

## Atualizando a Aplicação

Quando você faz alterações no código e quer atualizar os containers:

### 1. Rebuild da Aplicação

**Docker:**
```bash
# Reconstrói a imagem da aplicação e reinicia
docker-compose up -d --build clean_arch
```

**Podman:**
```bash
# Reconstrói a imagem da aplicação e reinicia
podman-compose up -d --build clean_arch
```

### 2. Atualizar Tudo (incluindo banco)

**Docker:**
```bash
# Para tudo, reconstrói e reinicia
docker-compose down
docker-compose up -d --build
```

**Podman:**
```bash
# Para tudo, reconstrói e reinicia
podman-compose down
podman-compose up -d --build
```

### 3. Apenas Reiniciar (sem rebuild)

Se você só quer reiniciar os containers sem reconstruir:

**Docker:**
```bash
docker-compose restart
```

**Podman:**
```bash
podman-compose restart
```

## Quando Usar Cada Comando?

- **`--build`**: Quando você alterou código Java, dependências (pom.xml) ou Dockerfile
- **Sem `--build`**: Quando só quer reiniciar os containers ou parar/iniciar
- **`down + up`**: Quando quer garantir um estado limpo

## Primeira Execução

Na primeira execução, o processo pode demorar mais tempo devido ao download das imagens:
- PostgreSQL 15: ~100MB
- Maven + OpenJDK 17: ~500MB
- Eclipse Temurin JRE: ~50MB

Após o download, as próximas execuções serão rápidas.