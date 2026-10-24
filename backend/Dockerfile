# --- Fase 1: Compilação (Build) ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Como o Root Directory é 'backend', o pom.xml e a pasta src estão logo aqui
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src src
RUN mvn clean package -DskipTests

# --- Fase 2: Execução ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]