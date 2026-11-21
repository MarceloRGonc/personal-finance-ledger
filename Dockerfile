FROM maven:3.9-eclipse-temurin-25@sha256:93b8a14ea2f412782e4e842651273b4d903e35cc496284f178fbbe2d67d00976 AS build

WORKDIR /build

COPY pom.xml .

RUN mvn -B -q dependency:go-offline

COPY src ./src

RUN mvn -B -q package -DskipTests \
    && mkdir -p /out/data /out/sqlite-native

FROM gcr.io/distroless/java25-debian13:nonroot@sha256:b0a31b9a063a6fb8132ec93da4cfd8515421ad35e40d7ac79fbebbaaae98227f

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
