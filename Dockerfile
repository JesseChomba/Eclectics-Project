# Stage 1: Build the Spring Boot application
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build the application
RUN mvn clean install -DskipTests

# Stage 2: Create the final lightweight runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app
# Copy the built JAR file from the build stage
COPY --from=build /app/target/smart-room-allocation-0.0.1-SNAPSHOT.jar smart-room-allocation.jar
# Expose the port your Spring Boot application runs on
EXPOSE 8080
# Run the application
ENTRYPOINT ["java", "-jar", "smart-room-allocation.jar"]