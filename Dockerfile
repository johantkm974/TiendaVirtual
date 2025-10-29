# ==============================
# Etapa 1: Construcción de la aplicación
# ==============================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos del módulo Maven
COPY demo/pom.xml demo/pom.xml

# Entrar al subproyecto
WORKDIR /app/demo

# Descargar dependencias (cacheable)
RUN mvn dependency:go-offline -B

# Copiar el código fuente completo
COPY demo /app/demo

# Compilar la aplicación (sin ejecutar tests)
RUN mvn clean package -DskipTests

# ==============================
# Etapa 2: Imagen final para ejecución
# ==============================
FROM eclipse-temurin:21-jdk

# Crear directorio de trabajo
WORKDIR /app

# Copiar el JAR generado desde la etapa anterior
COPY --from=builder /app/demo/target/*.jar app.jar

# Exponer el puerto (Railway asigna dinámicamente, pero 8080 es el estándar)
EXPOSE 8080

# ==============================
# Variables de entorno (Railway las inyecta automáticamente)
# ==============================
# Estas solo son valores por defecto, Railway las reemplaza por sus propias variables.
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql.railway.internal:3306/railway \
    SPRING_DATASOURCE_USERNAME=root \
    SPRING_DATASOURCE_PASSWORD=LofWYUWXAKncyfmRllDPPWcswLStwsyi \
    SPRING_JPA_HIBERNATE_DDL_AUTO=update \
    SPRING_JPA_SHOW_SQL=true

# ==============================
# Comando de inicio
# ==============================
ENTRYPOINT ["java", "-jar", "app.jar"]



