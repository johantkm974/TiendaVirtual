# Etapa 1: Compilar el proyecto con Maven
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copiar pom.xml y descargar dependencias (esto mejora la cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar sin ejecutar tests
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecutar el .jar generado
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copiar el .jar desde la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Exponer el puerto del contenedor
EXPOSE 8080

# Variables de entorno por defecto (Railway puede sobrescribirlas)
ENV SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/demo \
    SPRING_DATASOURCE_USERNAME=root \
    SPRING_DATASOURCE_PASSWORD=password \
    SPRING_JPA_HIBERNATE_DDL_AUTO=update \
    SPRING_JPA_SHOW_SQL=true \
    SERVER_PORT=8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]

