FROM maven:3.9-eclipse-temurin-26@sha256:b2c1ad85954592f9928e84327c65201f308ad9b5d8ed7d5b823717c97bf23fbb AS build

WORKDIR /build

COPY pom.xml .

RUN mvn -B -q dependency:go-offline

COPY src ./src

RUN mvn -B -q package -DskipTests \
    && mkdir -p /out/data /out/sqlite-native

FROM gcr.io/distroless/java25-debian13:nonroot@sha256:4618e7a1a1f5177222985347e1ffd503076f6be5837b89c8fd02800bd8551bde

WORKDIR /app

COPY --from=build --chown=65532:65532 /build/target/*.jar app.jar

COPY --from=build --chown=65532:65532 /out/data /app/data

COPY --from=build --chown=65532:65532 /out/sqlite-native /sqlite-native

ENV FINANCE_DATA_DIR=/app/data \
    SERVER_ADDRESS=0.0.0.0 \
    FINANCE_BACKUP_DIR=/app/data/backups

EXPOSE 8080

VOLUME /app/data

ENTRYPOINT ["/usr/bin/java", "-XX:MaxRAMPercentage=75", "-XX:+ExitOnOutOfMemoryError", "--enable-native-access=ALL-UNNAMED", "-Dorg.sqlite.tmpdir=/sqlite-native", "-jar", "/app/app.jar"]
