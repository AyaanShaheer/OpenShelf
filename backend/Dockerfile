# ------------------------------------------
# Stage 1: Build application
# ------------------------------------------
FROM maven:3.10.0-eclipse-temurin-21-noble AS build

WORKDIR /workspace

# Copy dependency definition first for better caching
COPY pom.xml .

# Download dependencies
RUN mvn -B -ntp dependency:go-offline

# Copy application source
COPY src ./src

# Compile, test and package
RUN mvn -B -ntp clean verify


# ------------------------------------------
# Stage 2: Production runtime
# ------------------------------------------
FROM eclipse-temurin:24.0.2_12-jre-noble AS runtime

WORKDIR /app

# Create non-root application user
RUN groupadd --system --gid 10001 spring \
    && useradd --system \
       --uid 10001 \
       --gid spring \
       --home-dir /app \
       --shell /usr/sbin/nologin spring

# Copy packaged application
COPY --from=build \
     --chown=10001:10001 \
     /workspace/target/openshelf-backend-0.1.0-SNAPSHOT.jar \
     /app/app.jar

ENV SERVER_PORT=8080

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
