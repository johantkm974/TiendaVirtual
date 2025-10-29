# Etapa 1: Construcción
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copiar pom y descargar dependencias
COPY demo/pom.xml demo/pom.xml
WORKDIR /app/demo
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY demo /app/demo

# Compilar sin tests
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copiar el jar generado
COPY --from=builder /app/demo/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Variables de entorno por defecto (Railway las puede sobreescribir)
ENV SPRING_DATASOURCE_URL=jdbc:mysql://yamanote.proxy.rlwy.net:40609/railway?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC \
    SPRING_DATASOURCE_USERNAME=root \
    SPRING_DATASOURCE_PASSWORD=LofWYUWXAKncyfmRllDPPWcswLStwsyi \
    SPRING_JPA_HIBERNATE_DDL_AUTO=update \
    SPRING_JPA_SHOW_SQL=true

ENTRYPOINT ["java", "-jar", "app.jar"]







