# Etapa 1: Construir con Maven
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copiar solo lo necesario
COPY demo/pom.xml demo/pom.xml
WORKDIR /app/demo
RUN mvn dependency:go-offline -B

# Copiar el código fuente completo
COPY demo /app/demo

# Compilar el proyecto
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final para ejecución
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/demo/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
