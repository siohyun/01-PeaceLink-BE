FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY build/libs/*-SNAPSHOT.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dserver.port=${PORT}", "app.jar"]