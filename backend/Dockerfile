FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar /app/app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]