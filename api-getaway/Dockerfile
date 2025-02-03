# Use OpenJDK as the base image
FROM openjdk:21-slim

# Set the environment timezone
ENV TZ="Asia/Phnom_Penh"

# Create the directories
RUN mkdir -p /home/deployment/jar

# Copy the JAR file from the build context
COPY target/api-getaway-0.0.1.jar /home/deployment/jar/app.jar

# Set the working directory
WORKDIR /home/deployment/jar

# Expose the HTTP port
EXPOSE 9015

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]