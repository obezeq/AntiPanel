# ========================================
# Multi-stage Dockerfile for Spring Boot 4 + Java 25
# Best practices: Multi-stage build, non-root user, minimal image
# ========================================

# ========================================
# Stage 1: Build Stage
# ========================================
FROM eclipse-temurin:25-jdk-jammy AS builder

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Ensure gradlew is executable (required for Linux/Unix)
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application (skip tests for faster builds)
# Use --no-daemon to avoid Gradle daemon in Docker
RUN ./gradlew clean bootJar --no-daemon -x test

# ========================================
# Stage 1.5: Test Stage (Optional)
# ========================================
FROM eclipse-temurin:25-jdk-jammy AS tester

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Ensure gradlew is executable (required for Linux/Unix)
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Note: Tests are NOT run during build - they run at container startup
# This allows tests to connect to the postgres-test database
# Tests are executed via docker-compose.test.yml command

# ========================================
# Stage 2: Runtime Stage
# ========================================
FROM eclipse-temurin:25-jre-jammy

# Metadata
LABEL maintainer="AntiPanel Team"
LABEL description="AntiPanel Backend - Spring Boot 4 + Java 25"
LABEL version="1.0.0"

# Set working directory
WORKDIR /app

# Install curl for health checks (minimal addition ~3MB)
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization flags for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
