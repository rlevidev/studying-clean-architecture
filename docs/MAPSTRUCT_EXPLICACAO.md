# MapStruct - Guia Completo com Exemplos Práticos

## O que é MapStruct?

MapStruct é uma **biblioteca de mapeamento de objetos** que gera código de mapeamento em tempo de compilação. Em vez de escrever manualmente código boilerplate para converter DTOs em entidades e vice-versa, o MapStruct cria esse código automaticamente.

## Por que usar MapStruct?

1. **Performance**: Gera código em tempo de compilação, não usa reflection
2. **Type-safe**: Erros de compilação ao mapear propriedades incompatíveis
3. **Menos código**: Elimina código boilerplate
4. **Manutenção**: Facilita refatorações

## Configuração Básica

### 1. Adicionar dependências ao pom.xml

```xml
<properties>
    <mapstruct.version>1.5.2.Final</mapstruct.version>
</properties>

<dependencies>
    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${mapstruct.version}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>${mapstruct.version}</version>
                    </path>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok-mapstruct-binding</artifactId>
                        <version>0.2.0</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Exemplos Práticos

### 1. Mapeamento Simples

**Entidade:**
```java
public record User(
    Long id,
    String name,
    String email,
    String password,
    Role role,
    LocalDateTime createdAt
) {}
```

**DTO:**
```java
public record UserResponse(
    Long id,
    String name,
    String email,
    Role role,
    LocalDateTime createdAt
) {}
```

**Mapper:**
```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    
    // Mapeamento simples - propriedades com mesmo nome
    UserResponse toResponse(User user);
    
    // Ignorar propriedades
    @Mapping(target = "password", ignore = true)
    UserResponse toResponseWithoutPassword(User user);
}
```

**Uso:**
```java
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id);
        return userMapper.toResponse(user);
    }
}
```

### 2. Mapeamento com Conversão de Tipos

**DTO com String ao invés de LocalDateTime:**
```java
public record UserResponse(
    Long id,
    String name,
    String email,
    Role role,
    String createdAt // String ao invés de LocalDateTime
) {}
```

**Mapper com conversão:**
```java
@Mapper(componentModel = "spring", uses = { DateMapper.class })
public interface UserMapper {
    
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    
    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "dd/MM/yyyy HH:mm:ss")
    UserResponse toResponse(User user);
}

@Mapper
public interface DateMapper {
    
    default String map(LocalDateTime date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : null;
    }
    
    default LocalDateTime map(String date) {
        return date != null ? LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : null;
    }
}
```

### 3. Mapeamento com Nomes Diferentes

**DTO com nomes diferentes:**
```java
public record UserResponse(
    Long userId,
    String userName,
    String userEmail,
    Role userRole,
    String creationDate
) {}
```

**Mapper com mapeamento de nomes:**
```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "userName", source = "name")
    @Mapping(target = "userEmail", source = "email")
    @Mapping(target = "userRole", source = "role")
    @Mapping(target = "creationDate", source = "createdAt", dateFormat = "dd/MM/yyyy")
    UserResponse toResponse(User user);
}
```

### 4. Mapeamento Bidirecional

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserResponse toResponse(User user);
    
    // Mapeamento inverso
    @InheritInverseConfiguration
    User toEntity(UserResponse response);
    
    // Ou manualmente:
    @Mapping(target = "id", ignore = true) // ID será gerado pelo banco
    @Mapping(target = "createdAt", ignore = true) // Data será setada no service
    User toEntity(UserResponse response);
}
```

### 5. Mapeamento com Validação

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "password", ignore = true) // Será criptografada no service
    User toEntity(UserRegisterRequest request);
    
    @Mapping(target = "password", ignore = true)
    UserResponse toResponse(User user);
}
```

### 6. Mapeamento com Coleções

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    List<UserResponse> toResponseList(List<User> users);
    
    // Para coleções grandes, pode ser mais eficiente usar stream
    default List<UserResponse> toResponseListStream(List<User> users) {
        return users.stream()
                   .map(this::toResponse)
                   .collect(Collectors.toList());
    }
}
```

### 7. Mapeamento com Valores Padrão

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "role", defaultValue = "USER")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    User toEntity(UserRegisterRequest request);
}
```

### 8. Mapeamento com Condicional

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "role", source = "role", qualifiedByName = "roleToEnum")
    UserResponse toResponse(User user);
    
    @Named("roleToEnum")
    default Role mapRole(String role) {
        return role != null ? Role.valueOf(role.toUpperCase()) : Role.USER;
    }
}
```

## Benefícios no Clean Architecture

### 1. Separação de Camadas
```java
// Camada de Infraestrutura - Conversão Entity <-> DTO
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserEntity toEntity(User user);
    User toDomain(UserEntity entity);
    UserResponse toResponse(User user);
}

// Camada de Use Case - Trabalha apenas com objetos de domínio
@Service
public class CreateUserUseCase {
    
    @Autowired
    private UserMapper userMapper;
    
    public UserResponse execute(UserRegisterRequest request) {
        User user = userMapper.toDomain(request);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }
}
```

### 2. Testabilidade
```java
@ExtendWith(MockitoExtension.class)
class UserMapperTest {
    
    @InjectMocks
    private UserMapper userMapper;
    
    @Test
    void shouldMapUserToResponse() {
        // Given
        User user = new User(1L, "John", "john@example.com", "password", Role.USER, LocalDateTime.now());
        
        // When
        UserResponse response = userMapper.toResponse(user);
        
        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("John");
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.role()).isEqualTo(Role.USER);
    }
}
```

## Dicas e Melhores Práticas

### 1. Organização de Mappers
```java
// Mappers específicos por camada
@Mapper(componentModel = "spring")
public interface UserDtoMapper {
    UserResponse toResponse(User user);
    User toDomain(UserRequest request);
}

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    UserEntity toEntity(User user);
    User toDomain(UserEntity entity);
}
```

### 2. Reutilização de Mappers
```java
@Mapper(componentModel = "spring", uses = { DateMapper.class, RoleMapper.class })
public interface UserMapper {
    UserResponse toResponse(User user);
}
```

### 3. Tratamento de Nulls
```java
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    @Mapping(target = "name", ignore = true)
    void updateUserFromDto(UserRequest request, @MappingTarget User user);
}
```

## Comparação: Manual vs MapStruct

### Código Manual (Antes)
```java
@Service
public class UserService {
    
    public UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt()
        );
    }
    
    public User toEntity(UserRequest request) {
        return new User(
            null, // ID será gerado
            request.getName(),
            request.getEmail(),
            encryptPassword(request.getPassword()),
            Role.USER,
            LocalDateTime.now()
        );
    }
}
```

### Código com MapStruct (Depois)
```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    
    UserResponse toResponse(User user);
    
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", defaultValue = "USER")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    User toEntity(UserRequest request);
}
```

## Conclusão

MapStruct é uma ferramenta poderosa que:

- **Reduz significativamente** a quantidade de código boilerplate
- **Aumenta a performance** com geração de código em tempo de compilação
- **Melhora a manutenção** com mapeamento declarativo
- **Facilita testes** com mapeamento explícito
- **Integra bem** com Spring Boot e Clean Architecture

É especialmente útil em projetos com muitos DTOs e entidades, como APIs REST, onde a conversão de objetos é uma tarefa frequente e repetitiva.