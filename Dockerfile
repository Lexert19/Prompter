FROM gradle:7.6-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew .
RUN gradle dependencies --no-daemon
COPY src src
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]