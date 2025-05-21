# Use Eclipse Temurin JDK 21 as base image (matches your pom.xml)
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first for better build caching
COPY .mvn .mvn
COPY mvnw pom.xml ./

# Download dependencies (will cache unless pom.xml changes)
RUN ./mvnw dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# Expose the port Spark uses
EXPOSE 4567

# Run the web server
CMD ["java", "-cp", "target/cyoa-game-1.0-SNAPSHOT.jar:target/dependency/*", "com.ajmoore00.cyoa.WebServer"]