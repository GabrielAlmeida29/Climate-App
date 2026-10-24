# --- Fase 1: Compilação (Build com Maven padrão) ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copia o pom.xml e descarrega as dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código fonte e faz o package
COPY src src
RUN mvn clean package -DskipTests

# --- Fase 2: Execução ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o jar gerado na fase anterior
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]