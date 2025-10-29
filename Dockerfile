# Etapa 1: Construir la app
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copiar pom y descargar dependencias
COPY demo/pom.xml demo/pom.xml
WORKDIR /app/demo
RUN mvn dependency:go-offline -B

# Copiar el código fuente completo
COPY demo /app/demo

# Compilar sin tests
RUN mvn clean package -DskipTests

# Etapa 2: Ejecutar
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copiar el jar generado
COPY --from=builder /app/demo/target/*.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Variables de entorno (Railway las inyecta automáticamente, pero puedes poner valores por defecto)
ENV MYSQLHOST=localhost \
    MYSQLPORT=3306 \
    MYSQLUSER=root \
    MYSQL_PASSWORD=password \
    MYSQL_DATABASE=demo

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]

