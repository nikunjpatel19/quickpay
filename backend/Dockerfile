FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew && ./gradlew buildFatJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build/libs/*-all.jar /app/app.jar

# Copy migration files into the final image (filesystem-based Flyway)
COPY --from=build /app/src/main/resources/db/migration /app/db/migration

CMD ["java", "-jar", "/app/app.jar"]
