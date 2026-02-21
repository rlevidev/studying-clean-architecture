# **LLM-Assisted Java Development Guidelines**

Este documento define padrões para projetos Java que incorporam geração de código por Large Language Models (LLMs). O objetivo é minimizar alucinações, deriva arquitetural, desorganização e limitações de contexto, maximizando correção, manutenibilidade e previsibilidade.

---

## **1. Tamanho de Arquivos e Granularidade de Código**

**1.1 Mantenha arquivos pequenos e focados**
Evite arquivos fonte monolíticos grandes. Divida a funcionalidade em módulos coerentes, tipicamente 100-400 linhas cada.

**1.2 Prefira muitos módulos pequenos a poucos grandes**
Arquivos menores reduzem confusão da LLM e melhoram a precisão das edições.

**1.3 Organize testes de forma paralela**
Testes unitários permanecem próximos ao código que validam (`src/test/java/...`).
Testes de integração ficam em diretórios separados.

---

## **2. Estrutura de Documentação para LLM**

**2.1 Mantenha documentos de engenharia claros e curtos**
Coloque todos os documentos de design e engenharia sob `docs/`.

**2.2 Use uma estrutura de diretórios previsível**

```
docs/
  overview.md
  design/
  subsystems/
  cheatsheets/
  prompts/
  adr/
```

**2.3 Forneça resumos de subsistemas**
Cada subsistema maior recebe um resumo curto e autocontido sob `docs/subsystems/`.

**2.4 Mantenha documentos concisos e explícitos**
Use seções curtas, definições explícitas e estrutura hierárquica rasa.
Evite prosa longa e ininterrupta.

---

## **3. Organização do Repositório**

**3.1 Mantenha o diretório raiz limpo**
O diretório raiz contém apenas arquivos essenciais: README, license, pom.xml/build.gradle, configurações de nível superior.

**3.2 Organize todo o código fonte sob `src/main/java/`**
Agrupar módulos em subdiretórios por domínio para evitar拥挤.
Exemplo (Clean Architecture):

```
src/
  main/
    java/
      com/
        exemplo/
          core/              # Camada de domínio (entities, usecases, gateways)
          infrastructure/    # Implementações concretas
  test/
    java/                     # Testes unitários e de integração
```

**3.3 Organize testes apropriadamente**

* Testes unitários permanecem próximos ao módulo testado.
* Testes de integração vão em `src/test/java/.../integration/`.

**3.4 Mantenha hierarquia de documentação**
Não coloque documentos soltos no raiz.

---

## **4. Cheat Sheets e Referência de Ambiente**

**4.1 Forneça resumos de ambiente**
Armazene detalhes de SO, versões de ferramentas e notas do sistema em:
`docs/cheatsheets/environment.md`

**4.2 Forneça cheat sheets de testes e ferramentas**
Inclua instruções para comandos de teste, ferramentas de cobertura e debugging sob:
`docs/cheatsheets/testing.md`

**4.3 Mantenha referência de comandos de shell**
Capture operações de arquivo e comandos comuns do ambiente de desenvolvimento em:
`docs/cheatsheets/filesystem.md`

**4.4 Forneça orientação de integração LLM**
Documente padrões de prompting, empacotamento de contexto e verificações de segurança em:
`docs/cheatsheets/llm_integration.md`

---

## **5. Convenções de Nome**

**5.1 Use nomes de arquivos normalizados**
Arquivos Java usam PascalCase para classes/records e snake_case para packages.
Evite nomenclatura inconsistente ou sufixos redundantes.

**5.2 Use nomes de diretórios previsíveis**
Nomes curtos, claros, em minúsculas com significado não ambíguo.

**5.3 Siga padrões de nomenclatura Java**

| Elemento | Convenção | Exemplo |
|----------|-----------|---------|
| Classes/Records | PascalCase | `UserService`, `UserResponse` |
| Interfaces | PascalCase | `UserGateway`, `UseCase` |
| Métodos/Variáveis | camelCase | `findById()`, `userRepository` |
| Constantes | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Packages | minúsculas | `com.exemplo.core.usecases` |

---

## **6. Estratégia de Testes**

**6.1 Enfatize cobertura completa de código**
Todo módulo deve ter cobertura abrangente incluindo:
- **Casos nominais**: Comportamento padrão esperado
- **Casos de borda**: Condições limite e valores especiais
- **Estados de erro**: Entradas inválidas e cenários de falha
- **Testes parametrizados**: Múltiplas entradas em um teste

**6.2 Alcance 100% de cobertura em caminhos críticos**
Funcionalidade核, código crítico de segurança e APIs públicas devem ter cobertura completa.
Use `mvn test`, `gradle test` ou ferramentas como JaCoCo para medir e aplicar cobertura.

**6.3 Prefira muitos testes curtos a poucos grandes**
Testes curtos e focados simplificam revisão e regeneração por LLMs.
Cada teste deve validar um comportamento ou invariante específico.

**6.4 Mantenha testes de integração orientados a cenário**
Testes de integração exercitam uso de APIs públicas e fluxos multi-módulos.
Cada teste de integração deve representar um cenário de uso realista.

**6.5 Use helpers de teste com moderação**
Utilitários compartilhados pertencem a classes auxiliares em `src/test/java/...`.
Evite criar frameworks de teste complexos que obscureçam os testes reais.

**6.6 Evite vazar internals para API pública**
Não exponha itens privados apenas para testar.
Use reflexão ou packages de teste para acessar membros privados.

**6.7 Documente requisitos de cobertura**
Cada módulo deve incluir comentário indicando cobertura esperada:
```java
// Test coverage: 100% (critical path)
// Tests: nominal, edge cases, error handling
```

---

## **7. Comentários e Documentação de Código**

**7.1 Escreva documentação abrangente em nível de módulo**
Cada módulo deve ter comentário de documentação explicando:
- Propósito e responsabilidades
- Estruturas de dados chave e seus invariantes
- Padrões de API pública e uso
- Estratégia de tratamento de erros

**7.2 Documente todos os itens públicos**
Métodos, classes, records e interfaces devem ter JavaDoc explicando:
- Propósito e comportamento
- Parâmetros e valores de retorno
- Condições de erro
- Exemplos onde apropriado

**7.3 Use comentários inline para lógica complexa**
Adicione comentários para explicar:
- Algoritmos não óbvios
- Regras de negócio críticas
- Considerações de performance
- Invariantes de segurança

**7.4 Mantenha estilo consistente de comentários**
Use `//` para comentários de linha, `/** */` para JavaDoc.
Mantenha comentários concisos mas informativos.

**7.5 Documente suposições e restrições**
Declare explicitamente suposições sobre:
- Validação de entrada
- Características de performance
- Uso de memória
- Thread safety

---

## **8. Organização de Arquivos Orientada a LLM**

**8.1 Prefira múltiplos arquivos pequenos a monólitos**
LLMs trabalham melhor com arquivos com menos de 400 linhas.
Divida módulos grandes em sub-módulos lógicos.

**8.2 Organize arquivos de teste sistematicamente**
Coloque arquivos de teste em estrutura paralela:
```
src/
  main/
    java/
      com/exemplo/
        service/
          UserService.java
  test/
    java/
      com/exemplo/service/
        UserServiceTest.java
        UserServiceIntegrationTest.java
```

**8.3 Mantenha arquivos de teste focados**
Cada arquivo de teste deve cobrir uma unidade lógica.
Evite criar arquivos de teste massivos e difíceis de navegar.

**8.4 Use nomes descritivos para arquivos de teste**
Nomeie arquivos de teste para refletir seu propósito:
- `UserServiceNominalTest.java` para comportamento padrão
- `UserServiceEdgeCaseTest.java` para condições de borda
- `UserServiceErrorTest.java` para cenários de erro

**8.5 Prefira classes de teste pequenas**
Uma classe de teste por cenário/método testado.
Evite "God Test Classes" com milhares de linhas.

---

## **9. Documentando Invariantes e Suposições**

**9.1 Registre invariantes para cada módulo**
Adicione invariantes em comentários ou em `docs/subsystems/<module>.md`.

**9.2 Defina estratégia de tratamento de erros**
Documente tipos de erro esperados, limites de exception e comportamento de recuperação.

**9.3 Esclareça regras de concorrência**
Se o sistema usa threads, async ou estado compartilhado, documente as regras explicitamente.

---

## **10. Práticas de Codificação Orientadas a LLM**

**10.1 Aplique mudanças um arquivo por vez**
Evite edições multi-arquivo grandes através de LLMs, a menos que necessário.

**10.2 Sempre forneça contexto completo**
LLMs produzem patches mais precisos quando recebem arquivos fonte completos, não fragmentos.

**10.3 Limite o escopo de mudança**
Especifique o que deve ser modificado e o que deve permanecer intocado.

**10.4 Valide saída imediatamente**
Compile, rode linters (Checkstyle, PMD), execute testes e verifique formatação após cada patch gerado por LLM.

**10.5 Mantenha exemplos canônicos**
Armazene exemplos canônicos de código para padrões como tratamento de erros, parsing, logging e transições de estado.
LLMs generalizam a partir desses padrões.

**10.6 Mantenha templates de prompt reutilizáveis**
Armazene templates de prompt para tarefas comuns sob `docs/prompts/`.

---

## **11. Estabilidade e Consistência Arquitetural**

**11.1 Mantenha limites de módulos estáveis**
Reorganização frequente aumenta chance de suposições incorretas da LLM.

**11.2 Use Architecture Decision Records (ADRs)**
Registre cada decisão maior sob `docs/adr/`.

**11.3 Padronize pipeline de validação de código**
Crie scripts que executem:

* Formatação (Spotless, Google Java Format)
* Linting (Checkstyle, PMD, SonarQube)
* Compilação
* Testes unitários (JUnit 5)
* Testes de integração
* Cobertura (JaCoCo, Cobertura)

**11.4 Limpeza periódica de testes gerados**
Revise suites de teste e remova testes redundantes ou excessivamente específicos.

**11.5 Mantenha exemplo de código pequeno e preciso**
Exemplos devem ser fáceis de carregar no contexto da LLM.

---

## **12. Ferramentas Java Recomendadas**

### Build e Gerenciamento
- **Maven 3.8+** ou **Gradle 8+**
- **Java 17+** (LTS recomendado)

### Qualidade de Código
- **Spotless** ou **Google Java Format** (formatação)
- **Checkstyle** ou **PMD** (análise estática)
- **SonarQube** (análise de qualidade)

### Testes
- **JUnit 5** (testes unitários)
- **Mockito** (mocking)
- **AssertJ** (assertions fluentes)
- **Testcontainers** (testes de integração com banco)

### Cobertura
- **JaCoCo** (análise de cobertura)

---

## **13. Resumo**

O propósito destas diretrizes é estruturar o desenvolvimento para que a geração de código via LLM se torne previsível e robusta. Mantendo arquivos pequenos, documentação clara, nomenclatura consistente e decisões arquiteturais explícitas, o projeto permanece gerenciável tanto para humanos quanto para sistemas automatizados.

---

## **Exemplo de Estrutura de Projeto Java**

```
projeto-java/
├── pom.xml
├── README.md
├── LICENSE
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── exemplo/
│   │   │           ├── StudyingCleanArchitectureApplication.java
│   │   │           ├── core/
│   │   │           │   ├── entities/
│   │   │           │   ├── enums/
│   │   │           │   ├── exception/
│   │   │           │   ├── gateway/
│   │   │           │   ├── usecases/
│   │   │           │   └── utils/
│   │   │           └── infrastructure/
│   │   │               ├── beans/
│   │   │               ├── dto/
│   │   │               ├── exception/
│   │   │               ├── gateway/
│   │   │               ├── mapper/
│   │   │               ├── persistence/
│   │   │               ├── presentation/
│   │   │               └── security/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── exemplo/
│       │           ├── core/
│       │           │   └── usecases/
│       │           └── infrastructure/
│       └── resources/
├── docs/
│   ├── overview.md
│   ├── design/
│   ├── subsystems/
│   ├── cheatsheets/
│   │   ├── environment.md
│   │   ├── testing.md
│   │   └── filesystem.md
│   ├── prompts/
│   └── adr/
├── .kilocode/
│   └── rules/
│       └── memory-bank/
├── docker-compose.yml
└── Dockerfile
```
