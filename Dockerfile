


# Stage 1: Build the application
FROM gradle:8.5-jdk21-alpine AS build
LABEL authors="njabulokumalo"
WORKDIR /app

# Copy only the files needed for dependency resolution to leverage Docker caching
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Grant execution rights on the wrapper and download dependencies
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# Copy the rest of the source code and build the JAR
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Gradle puts the JAR in build/libs/ instead of target/
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

# Keep the memory limits for Render's free tier
ENTRYPOINT ["java", "-Xmx400m", "-Xms256m", "-jar", "app.jar"]