# ==============================
# Etapa 1: Construcción con Maven
# ==============================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Directorio de trabajo
WORKDIR /app

# Copiar pom.xml y descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# ==============================
# Etapa 2: Imagen final
# ==============================
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copiar el jar desde la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Puerto expuesto (Railway lo detecta automáticamente)
EXPOSE 8080

# Variables de entorno (Railway las sobreescribe)
ENV SPRING_PROFILES_ACTIVE=prod

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]
