# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Variables de entorno - CRÍTICO
ENV SPRING_PROFILES_ACTIVE=docker

# Ejecutar aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]