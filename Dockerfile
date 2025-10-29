# Imagen base con Java y Maven preinstalado
FROM maven:3.9.9-eclipse-temurin-21

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar el pom.xml y descargar dependencias primero (cache eficiente)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el resto del código fuente
COPY . .

# Compilar y crear el .jar (sin ejecutar tests)
RUN mvn clean package -DskipTests

# Exponer el puerto donde corre la app
EXPOSE 8080

# Comando de inicio
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]
