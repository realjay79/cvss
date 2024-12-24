# Use a specific Gradle image with Java 17 and Gradle 8.5 to build the application
FROM gradle:8.5-jdk17 AS builder

# Switch to root user to fix permissions
USER root
RUN mkdir -p /home/gradle/.gradle && chown -R gradle:gradle /home/gradle

# Switch back to gradle user
USER gradle

# Copy the Gradle wrapper and project files
COPY --chown=gradle:gradle . /home/gradle/project

# Set the working directory
WORKDIR /home/gradle/project

# Build the application using Gradle
RUN gradle build --no-daemon --refresh-dependencies --stacktrace

# Use the Amazon Corretto image with Java 17 as the base image for the runtime
FROM amazoncorretto:17

# Add a volume pointing to /tmp
VOLUME /tmp

# Expose port 8080
EXPOSE 8080

# Copy the JAR file from the build stage
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

# Run the application
ENTRYPOINT ["java","-jar","/app.jar"]
