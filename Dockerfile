# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Maven project file and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the application source code
COPY src ./src

# Build the application into a JAR file
RUN mvn package

# Specify the command to run on container startup
CMD ["java", "-jar", "target/cloud-simulator-1.0-SNAPSHOT.jar"]