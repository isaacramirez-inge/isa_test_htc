# Use WildFly with Java 17
FROM quay.io/wildfly/wildfly:29.0.1.Final-jdk17

# Note: Using curl instead of wget for health check (usually pre-installed)

# Set environment variables
ENV WILDFLY_HOME=/opt/jboss/wildfly
ENV DEPLOYMENT_DIR=$WILDFLY_HOME/standalone/deployments

# Copy the WAR file
COPY target/transaction-frontend.war $DEPLOYMENT_DIR/

# Create deployment marker to auto-deploy
RUN touch $DEPLOYMENT_DIR/transaction-frontend.war.dodeploy

# Add management user (admin/admin)
RUN $WILDFLY_HOME/bin/add-user.sh -u admin -p admin --silent

# Create directories with correct permissions for volumes
RUN mkdir -p $WILDFLY_HOME/standalone/data && \
    mkdir -p $WILDFLY_HOME/standalone/log && \
    chown -R jboss:jboss $WILDFLY_HOME/standalone/data && \
    chown -R jboss:jboss $WILDFLY_HOME/standalone/log

# Configure WildFly for production
# Using default WildFly configuration

# Expose HTTP port
EXPOSE 8080

# Expose management port (optional, for monitoring)
EXPOSE 9990

# Set JVM options optimized for containers
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true"

# Health check - check if WildFly is responding and app is deployed
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:8080/transaction-frontend/ || exit 1

# Start WildFly in standalone mode
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]

# Labels for metadata
LABEL maintainer="Transaction System Team"
LABEL description="JSF Frontend for Transaction Management System running on WildFly"
LABEL version="0.0.1-SNAPSHOT"