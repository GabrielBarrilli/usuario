FROM gradle:8.7-jdk17 as build
WORKDIR /app
COPY . .

RUN gradle clean bootJar --no-daemon --stacktrace

FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/usuario.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/usuario.jar"]
