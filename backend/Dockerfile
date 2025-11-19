# Compilar el jar con Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar pom.xml y resolver dependencias
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copiar el código fuente
COPY src ./src

# Compilar el jar
RUN mvn -q -DskipTests clean package

# Imagen ligera solo con el JRE
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el jar generado
COPY --from=build /app/target/*.jar app.jar

# Puerto donde expone el backend
EXPOSE 8080

# Arrancar Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
