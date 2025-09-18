# Use Eclipse Temurin (AdoptOpenJDK) with Java 17
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Create user for security (don't run as root)
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy the JAR file
COPY target/backend-project-0.0.1-SNAPSHOT.jar app.jar

# Change ownership of the app directory to appuser
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/api/transactions/health || exit 1

# Set JVM options for container environment
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels for metadata
LABEL maintainer="Transaction System Team"
LABEL description="Spring Boot Backend for Transaction Management System"
LABEL version="0.0.1-SNAPSHOT"