# Use Eclipse Temurin JDK 21 as base image (matches your pom.xml)
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven

# Copy pom.xml and download dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application (skip tests for faster build)
RUN mvn clean package -DskipTests

# Expose the port Spark uses
EXPOSE 4567

# Run the web server
CMD ["java", "-cp", "target/cyoa-game-1.0-SNAPSHOT.jar:target/dependency/*", "com.ajmoore00.cyoa.WebServer"]