FROM eclipse-temurin:22-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Install Maven and dependencies
RUN apk add --no-cache maven

# Copy only pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage - use Alpine for smaller size
FROM eclipse-temurin:22-jre-alpine

# Set working directory
WORKDIR /app

# Install curl for health check
RUN apk add --no-cache curl

# Create uploads directory
RUN mkdir -p uploads

# Copy the built JAR from builder stage
COPY --from=builder /app/target/p2p-1.0-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SERVER_PORT=8080
ENV FILE_UPLOAD_DIR=./uploads

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 
