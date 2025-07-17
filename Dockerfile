# Etapa 1: build com Maven + JDK 17
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copia arquivos de build
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Baixa dependências
RUN ./mvnw dependency:go-offline -B

# Copia código fonte e arquivos liquibase
COPY src src
COPY liquibase liquibase

# Empacota o projeto
RUN ./mvnw clean package -DskipTests -B

# Etapa 2: runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
