# Etapa 1: Compilar el proyecto con Maven
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copiar pom.xml y descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecutar el .jar generado
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiar el .jar desde la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Crear usuario no-root para seguridad
RUN useradd -m myapp
USER myapp

# Exponer el puerto del contenedor
EXPOSE 8080

# Comando de inicio con debugging
ENTRYPOINT ["java", "-jar", "app.jar"]
