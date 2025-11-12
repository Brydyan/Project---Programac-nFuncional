# --- Etapa 1: Compilación (Build) ---
# Usamos una imagen con Maven y Java 21 (para coincidir con tu pom.xml)
FROM maven:3-eclipse-temurin-21-jammy AS builder

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos solo el pom.xml para descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el resto del código fuente y compilamos
COPY src ./src
RUN mvn clean package -DskipTests

# --- Etapa 2: Ejecución (Run) ---
# Usamos una imagen ligera que solo tenga Java 21 (JRE)
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copiamos el .jar compilado
# (Spring Boot crea un solo .jar, así que esto debería funcionar)
COPY --from=builder /app/target/*.jar app.jar

# Exponemos el puerto de Spring Boot (default 8080)
EXPOSE 8080

# Comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]