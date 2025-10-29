# Imagen base de Java 21
FROM eclipse-temurin:21-jdk

# Crear directorio de trabajo
WORKDIR /app

# Copiar el código fuente
COPY . .

# Construir la aplicación (usa el Maven Wrapper incluido)
RUN ./mvnw clean package -DskipTests

# Exponer el puerto donde corre Spring Boot
EXPOSE 8080

# Comando para iniciar la app
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]
