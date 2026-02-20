# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY src ./src

RUN mvn clean package -DskipTests && \
    JAR_PATH=$(find target -maxdepth 1 -type f -name "*.jar" ! -name "*original*" | head -n 1) && \
    cp "$JAR_PATH" target/app.jar

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

COPY --from=build /workspace/target/app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:InitialRAMPercentage=25.0", "-XX:MaxRAMPercentage=75.0", "-XX:+UseStringDeduplication", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
