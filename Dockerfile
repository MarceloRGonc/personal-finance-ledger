# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests

FROM eclipse-temurin:25-jre
RUN useradd --system --no-create-home --shell /usr/sbin/nologin --uid 10001 finances \
    && mkdir -p /app/data \
    && chown finances:finances /app/data
WORKDIR /app
COPY --from=build --chown=finances:finances /build/target/*.jar app.jar
USER finances
ENV FINANCE_DATA_DIR=/app/data \
    SERVER_ADDRESS=0.0.0.0 \
    FINANCE_BACKUP_DIR=/app/data/backups
EXPOSE 8080
VOLUME /app/data
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080"]
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
