# Build stage
FROM docker.io/library/maven:3.8.4-openjdk-17 AS build
WORKDIR /app
# Copiar apenas o pom.xml primeiro para aproveitar o cache do Docker nas dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar o código fonte e realizar o build
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM docker.io/library/eclipse-temurin:17-jre-alpine
WORKDIR /app

# Criar um usuário não-root para executar a aplicação por questões de segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
