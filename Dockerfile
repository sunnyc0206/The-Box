# Multi-stage build for Spring Boot backend
FROM eclipse-temurin:22-jdk-alpine AS builder
WORKDIR /app
RUN apk add --no-cache maven
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage - use Alpine for smaller size
FROM eclipse-temurin:22-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl

# Create uploads directory (if needed by your application)
RUN mkdir -p uploads

# Copy the built JAR from builder stage
COPY --from=builder /app/target/thebox-backend-1.0.0.jar app.jar

# Expose port (8080 is the default for Spring Boot)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application 
CMD ["java", "-jar", "app.jar"]
