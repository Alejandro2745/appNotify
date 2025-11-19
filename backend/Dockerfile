# 1) Etapa de build: compilar el jar con Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos pom.xml y resolvemos dependencias primero (cache más eficiente)
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Ahora copiamos el código fuente
COPY src ./src

# Compilamos el jar
RUN mvn -q -DskipTests clean package

# 2) Etapa de runtime: imagen ligera solo con el JRE
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiamos el jar generado
COPY --from=build /app/target/*.jar app.jar

# Puerto donde expone el backend (ajusta si tu app usa otro)
EXPOSE 8080

# Arrancar Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
