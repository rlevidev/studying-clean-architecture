# Correção do Endpoint de Registro - `/api/v1/auth/register`

## Problema Original

Ao fazer uma requisição POST para o endpoint `http://localhost:8080/api/v1/auth/register`, o sistema retornava:

```json
{
  "error": "Not authenticated",
  "message": "You need to be logged in to access this feature.",
  "status": 401
}
```

Isso era inconsistente, pois o endpoint de registro deveria ser público e não exigir autenticação.

## Diagnóstico e Causas Raiz

### 1. Problema de Mapeamento de Rotas no Spring Security 6

**O que foi identificado:**
No `SecurityConfig.java`, a configuração de segurança usava `requestMatchers()` com Strings simples:

```java
.requestMatchers(
    "/api/v1/auth/register", 
    "/api/v1/auth/login", 
    "/api/v1/auth/refresh"
).permitAll()
```

**Por que isso era problema:**
No Spring Security 6, o comportamento padrão de `requestMatchers()` mudou. Quando usado com strings simples, o Spring Security tenta inferir o tipo de request matcher, mas isso pode levar a falhas no matching de requisições, especialmente quando há redirecionamentos internos.

### 2. Rota `/error` Não Protegida Causando 401

**O que foi identificado:**
O endpoint `/error` não estava configurado como público na cadeia de segurança.

**Por que isso era problema:**
Quando ocorre qualquer erro durante o processamento de uma requisição (como erro de validação de campos, JSON malformado, etc.), o Spring redireciona internamente para `/error` para gerar a resposta de erro. Se essa rota não estiver marcada como `permitAll()`, o Spring Security intercepta esse redirecionamento e exige autenticação, resultando em um 401 em vez de mostrar o erro real.

### 3. JwtFilter Registrado Duas Vezes

**O que foi identificado:**
O `JwtFilter` era um `@Component` e estava sendo injetado no `SecurityConfig` via `@Autowired`.

**Por que isso era problema:**
O Spring Boot registra automaticamente todos os beans `@Filter` no contexto do servlet. Como o filtro também estava sendo adicionado explicitamente à cadeia de segurança do Spring Security via `addFilterBefore()`, ele estava sendo executado duas vezes:
1. Uma vez como filtro comum do servlet (antes da cadeia de segurança)
2. Uma vez na cadeia de segurança do Spring

Isso causava comportamento imprevisível e possíveis conflitos de processamento de tokens.

### 4. Falha na Validação de Token no JwtFilter

**O que foi identificado:**
No `JwtFilter.java`, a extração do username do token estava fora do bloco try-catch:

```java
String token = authHeader.substring(7);
String username = jwtUtil.extractUsername(token);  // FORA do try-catch

try {
    if (jwtUtil.validateToken(token, username)) {
        // ...
    }
} catch (Exception e) {
    // tratamento de erro
}
```

**Por que isso era problema:**
Se o token estivesse malformado, expirado ou inválido, o método `extractUsername()` podia lançar uma exceção ANTES de entrar no bloco try-catch. Isso causava uma exceção não tratada que propagava para cima e podia ser interpretada como erro de autenticação.

### 5. Descompasso entre Banco de Dados e JPA

**O que foi identificado:**
A migration `V4__create_refresh_tokens_table.sql` definia a coluna `id` como:

```sql
id SERIAL PRIMARY KEY,
```

Mas a entidade JPA `RefreshTokenEntity` tinha:

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

**Por que isso era problema:**
O tipo `SERIAL` no PostgreSQL cria automaticamente uma coluna do tipo `INTEGER`, mas o JPA espera um `BIGINT` para o tipo `Long`. Isso causava incompatibilidade de tipos entre o banco de dados e o mapeamento ORM, podendo levar a erros durante a inserção de registros.

### 6. Associação de Usuário em RefreshToken Nula

**O que foi identificado:**
No `RefreshTokenRepositoryGateway.java`, ao salvar um novo refresh token, o código era:

```java
@Override
public RefreshToken save(RefreshToken refreshToken) {
    RefreshTokenEntity entity = refreshTokenMapper.toEntity(refreshToken);
    RefreshTokenEntity saved = refreshTokenRepository.save(entity);
    return refreshTokenMapper.toDomain(saved);
}
```

**Por que isso era problema:**
O método `toEntity()` não estava associando o `UserEntity` ao `RefreshTokenEntity`, deixando o campo `user_id` como `null`. Como a tabela `refresh_tokens` tinha uma restrição `NOT NULL` na coluna `user_id`, qualquer tentativa de inserção falhava com erro de constraint violada.

### 7. Configuração Inconsistente de JWT

**O que foi identificado:**
Em `application.properties`:
```properties
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_ACCESS_TOKEN_EXPIRATION}
jwt.refresh.expiration=${JWT_REFRESH_TOKEN_EXPIRATION}
```

No `JwtUtil.java`:
```java
@Value("${JWT_SECRET}")  // INCONSISTENTE
private String secret;

@Value("${jwt.access.expiration}")  // CONSISTENTE
private Long accessTokenExpiration;
```

**Por que isso era problema:**
A propriedade `JWT_SECRET` (sem prefixo `jwt.`) não correspondia ao que estava definido no `application.properties`. Além disso, não havia valores padrão, então se as variáveis de ambiente não estivessem definidas, a aplicação não iniciava com erro de valor não resolvido.

---

## Soluções Implementadas

### 1. Mapeamento de Rotas com AntPathRequestMatcher

**Arquivo:** `src/main/java/com/rlevi/studying_clean_architecture/infrastructure/security/SecurityConfig.java`

**Alteração:**
```java
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

// ...

.authorizeHttpRequests(authorize -> authorize
    .requestMatchers(
        AntPathRequestMatcher.antMatcher("/api/v1/auth/**"),
        AntPathRequestMatcher.antMatcher("/error")
    ).permitAll()
    .anyRequest().authenticated())
```

**Como funciona:**
- `AntPathRequestMatcher.antMatcher("/api/v1/auth/**")` garante que TODAS as rotas começando com `/api/v1/auth/` (incluindo `/api/v1/auth/register`, `/api/v1/auth/login`, `/api/v1/auth/refresh`) sejam públicas.
- `AntPathRequestMatcher.antMatcher("/error")` garante que a rota de tratamento de erros seja pública.
- O uso de `AntPathRequestMatcher` é mais explícito e confiável no Spring Security 6.

**Por que foi feito:**
- Elimina ambiguidades no matching de rotas
- Garante que redirecionamentos internos para `/error` não sejam bloqueados por segurança
- Usa o método recomendado para Spring Security 6+

### 2. Desabilitação de Registro Duplo do JwtFilter

**Arquivo:** `src/main/java/com/rlevi/studying_clean_architecture/infrastructure/security/SecurityConfig.java`

**Alteração:**
```java
@Bean
public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter filter) {
    FilterRegistrationBean<JwtFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
}
```

**Como funciona:**
- O `JwtFilter` continua sendo injetado no `SecurityConfig` via `@Autowired` (é necessário para o `addFilterBefore()`)
- O método `jwtFilterRegistration()` cria um `FilterRegistrationBean` que registra o filtro no contexto do servlet
- `registration.setEnabled(false)` desabilita esse registro automático
- Assim, o filtro só executa na cadeia de segurança do Spring Security (via `addFilterBefore()`)

**Por que foi feito:**
- Evita execução duplicada do filtro
- Garante processamento consistente de tokens
- Elimina possíveis conflitos de segurança

### 3. Correção no Tratamento de Token no JwtFilter

**Arquivo:** `src/main/java/com/rlevi/studying_clean_architecture/infrastructure/security/JwtFilter.java`

**Alteração:**
```java
if (authHeader != null && authHeader.startsWith("Bearer ")) {
    String token = authHeader.substring(7);

    try {
        String username = jwtUtil.extractUsername(token);  // MOVIDO para DENTRO do try-catch
        if (jwtUtil.validateToken(token, username)) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
    } catch (Exception e) {
        // tratamento de erro continua o mesmo
    }
}
```

**Como funciona:**
- A extração do username agora está DENTRO do bloco try-catch
- Se `extractUsername()` lançar exceção (token malformado, expirado, etc.), a exceção é capturada
- O filtro continua sua execução normalmente, permitindo que a requisição prossiga

**Por que foi feito:**
- Garante que erros de token não causem falhas não tratadas
- Permite que endpoints públicos funcionem mesmo com tokens inválidos
- Mantém a segurança intacta para endpoints que realmente exigem autenticação

### 4. Migration para Corrigir Tipo da Coluna ID

**Arquivo:** `src/main/resources/db/migration/V5__fix_refresh_tokens_id_type.sql`

**Conteúdo:**
```sql
ALTER TABLE refresh_tokens ALTER COLUMN id TYPE BIGINT;
```

**Como funciona:**
- Flyway executa essa migration automaticamente ao iniciar a aplicação
- Altera o tipo da coluna `id` de `INTEGER` (criado pelo `SERIAL`) para `BIGINT`
- Isso alinha o tipo do banco com o tipo `Long` do Java

**Por que foi feito:**
- Elimina incompatibilidade entre banco de dados e JPA
- Garante que o mapeamento ORM funcione corretamente
- Permite uso correto de `GenerationType.IDENTITY`

### 5. Associação de Usuário ao RefreshToken

**Arquivo:** `src/main/java/com/rlevi/studying_clean_architecture/infrastructure/gateway/RefreshTokenRepositoryGateway.java`

**Alterações:**
```java
@Component
public class RefreshTokenRepositoryGateway implements RefreshTokenGateway {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;  // NOVA dependência
    private final RefreshTokenMapper refreshTokenMapper;

    public RefreshTokenRepositoryGateway(
        RefreshTokenRepository refreshTokenRepository, 
        UserRepository userRepository,  // NOVO parâmetro
        RefreshTokenMapper refreshTokenMapper
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.refreshTokenMapper = refreshTokenMapper;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        // Carrega o UserEntity pelo ID
        UserEntity userEntity = userRepository.getReferenceById(refreshToken.userId());
        // Usa o método de mapeamento que inclui o usuário
        RefreshTokenEntity entity = refreshTokenMapper.toEntityWithUser(refreshToken, userEntity);
        RefreshTokenEntity saved = refreshTokenRepository.save(entity);
        return refreshTokenMapper.toDomain(saved);
    }
}
```

**Como funciona:**
- Injeta o `UserRepository` para poder carregar a entidade de usuário
- Usa `userRepository.getReferenceById()` para obter uma referência ao usuário (mais eficiente que carregar o objeto completo)
- Chama um novo método `toEntityWithUser()` no mapper que recebe o `UserEntity`
- O `RefreshTokenEntity` é salvo com a associação correta ao usuário

**Arquivo:** `src/main/java/com/rlevi/studying_clean_architecture/infrastructure/mapper/RefreshTokenMapper.java`

**Novo método adicionado:**
```java
public RefreshTokenEntity toEntityWithUser(RefreshToken refreshToken, UserEntity userEntity) {
    RefreshTokenEntity entity = toEntity(refreshToken);
    entity.setUser(userEntity);
    return entity;
}
```

**Por que foi feito:**
- Garante que o campo `user_id` tenha um valor válido ao criar um refresh token
- Respeita a constraint `NOT NULL` da tabela
- Mantém a integridade referencial entre usuários e tokens de atualização

### 6. Correção da Configuração de JWT

**Arquivo:** `src/main/resources/application.properties`

**Alteração:**
```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b}
jwt.access.expiration=${JWT_ACCESS_TOKEN_EXPIRATION:3600000}
jwt.refresh.expiration=${JWT_REFRESH_TOKEN_EXPIRATION:86400000}
```

**Arquivo:** `src/main/java/com/rlevi/studying_clean_architecture/infrastructure/security/JwtUtil.java`

**Alteração:**
```java
@Value("${jwt.secret}")  // CORRIGIDO (antes era ${JWT_SECRET})
private String secret;

@Value("${jwt.access.expiration}")
private Long accessTokenExpiration;
```

**Como funciona:**
- Todas as propriedades agora têm o prefixo `jwt.` consistente
- A sintaxe `${VARIAVEL:valor_padrao}` define um valor padrão caso a variável de ambiente não exista
- `jwt.secret` usa uma chave padrão de 256 bits (64 caracteres hexadecimais)
- `jwt.access.expiration` tem padrão de 1 hora (3600000 ms = 60 * 60 * 1000)
- `jwt.refresh.expiration` tem padrão de 24 horas (86400000 ms = 24 * 60 * 60 * 1000)

**Por que foi feito:**
- Garante que a aplicação inicie mesmo sem variáveis de ambiente definidas
- Elimina inconsistências de nomenclatura entre `application.properties` e `JwtUtil.java`
- Fornece valores padrão seguros para desenvolvimento

### 7. Habilitação do CORS

**Arquivo:** `src/main/java/com/rlevi/studying_clean_architecture/infrastructure/security/SecurityConfig.java`

**Alteração:**
```java
.cors(cors -> cors.configurationSource(corsConfigurationSource()))
```

**Método adicionado (ou corrigido):**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("*"));
    configuration.setAllowedMethods(List.of("*"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**Como funciona:**
- Configura CORS para permitir requisições de qualquer origem
- Permite todos os métodos HTTP (GET, POST, PUT, DELETE, etc.)
- Permite todos os headers
- Habilita credenciais (necessário para autenticação)
- Aplica a todas as rotas (`/**`)

**Por que foi feito:**
- Permite que clientes web (como Bruno, Postman, frontends React/Vue) acessem a API
- Evita erros de CORS ao fazer requisições de origens diferentes
- Configuração padrão adequada para desenvolvimento

---

## Resultado Final

Após todas as correções, o endpoint de registro funciona corretamente:

**Requisição:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
-H "Content-Type: application/json" \
-d '{
  "name": "Success Test",
  "email": "successtest@example.com",
  "password": "password123"
}'
```

**Resposta:**
```json
{
  "message": "User registered successfully.",
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Status:** `200 OK`

---

## Lições Aprendidas

### Spring Security 6

1. **Use `AntPathRequestMatcher` explicitamente** para evitar ambiguidades no matching de rotas
2. **Sempre inclua `/error`** em `permitAll()` para permitir tratamento adequado de erros
3. **Evite registro duplo de filtros** usando `FilterRegistrationBean.setEnabled(false)`

### JPA e Banco de Dados

1. **Alinhe tipos entre DB e JPA**: `SERIAL` cria `INTEGER`, mas JPA `Long` precisa de `BIGINT`
2. **Gerencie associações manualmente** ao criar entidades que têm relacionamentos
3. **Use migrations para correções de schema** em vez de alterar manualmente o banco

### Configuração

1. **Use valores padrão** em `application.properties` com sintaxe `${VAR:default}`
2. **Mantenha consistência de nomenclatura** entre `application.properties` e `@Value`
3. **Trate exceções adequadamente** dentro de filtros para evitar falhas não tratadas

### Clean Architecture

- A correção respeitou os princípios da arquitetura limpa:
  - `Infrastructure layer` ajustada sem alterar `Domain layer`
  - `Gateways` mantiveram a integridade do domínio
  - `Use cases` não foram afetados pelas mudanças de infraestrutura

---

## Checklist de Validação

- [x] Endpoint `/api/v1/auth/register` é público (200 OK)
- [x] Endpoint `/api/v1/auth/login` é público
- [x] Endpoint `/api/v1/auth/refresh` é público
- [x] Endpoints protegidos retornam 401 quando sem token
- [x] Tokens inválidos são tratados adequadamente
- [x] Refresh tokens são salvos corretamente com associação de usuário
- [x] Banco de dados e JPA estão alinhados
- [x] Aplicação inicia sem variáveis de ambiente (usa defaults)
- [x] CORS está configurado para permitir requisições externas
- [x] Logs da aplicação não mostram erros relacionados a segurança

---

## Arquivos Modificados

1. `src/main/java/com/rlevi/studying_clean_architecture/infrastructure/security/SecurityConfig.java`
2. `src/main/java/com/rlevi/studying_clean_architecture/infrastructure/security/JwtFilter.java`
3. `src/main/java/com/rlevi/studying_clean_architecture/infrastructure/security/JwtUtil.java`
4. `src/main/java/com/rlevi/studying_clean_architecture/infrastructure/gateway/RefreshTokenRepositoryGateway.java`
5. `src/main/java/com/rlevi/studying_clean_architecture/infrastructure/mapper/RefreshTokenMapper.java`
6. `src/main/resources/application.properties`
7. `src/main/resources/db/migration/V5__fix_refresh_tokens_id_type.sql` (novo)
