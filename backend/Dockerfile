# --- Fase 1: Compilação (Build) ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copia os ficheiros do Maven e o código fonte
mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
COPY src src

# Dá permissão de execução ao Maven Wrapper e compila o projeto
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# --- Fase 2: Execução ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o jar gerado na fase anterior para a imagem final
COPY --from=build /app/target/*.jar app.jar

# Define a porta que a aplicação vai escutar
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]