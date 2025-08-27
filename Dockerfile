# Use a base image that includes both Maven and JDK 21
FROM maven:3.9-eclipse-temurin-21 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the Maven project file and download dependencies first for better caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the application source code
COPY src ./src

# Build the application into a JAR file
RUN mvn package -DskipTests

# --- Second Stage: Create a smaller final image ---

# Use a slim Java runtime image for the final container
FROM openjdk:21-jre-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the 'build' stage
COPY --from=build /app/target/cloud-simulator-1.0-SNAPSHOT.jar .

# Expose the port the application runs on
EXPOSE 7070

# Specify the command to run on container startup
CMD ["java", "-jar", "cloud-simulator-1.0-SNAPSHOT.jar"]
