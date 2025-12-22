FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

COPY src/ ./src/
COPY lib ./lib
COPY config.json ./

# Compile all .java files into a "bin" directory
RUN mkdir out && \
    javac -d out \
    -sourcepath src \
    -cp "lib/*" \
    $(find src -name "*.java")

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd -r appuser && useradd -r -g appuser appuser

COPY --chown=appuser:appuser --from=builder /app/out ./classes
COPY --chown=appuser:appuser --from=builder /app/lib ./lib
COPY --chown=appuser:appuser --from=builder /app/config.json ./config.json

RUN touch log.txt && chown appuser:appuser log.txt
USER appuser

CMD ["java", "-cp", "classes:lib/*", "ru.yandex.practicum.ServerHMAC"]
