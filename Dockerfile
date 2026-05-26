FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .

RUN chmod +x ./gradlew

# Gradle dependency cache
RUN ./gradlew dependencies || true

COPY . .

ARG SERVICE_NAME
RUN ./gradlew :${SERVICE_NAME}:clean :${SERVICE_NAME}:bootJar -x test

FROM eclipse-temurin:17-jre
WORKDIR /app

ARG SERVICE_NAME
COPY --from=build /app/${SERVICE_NAME}/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]