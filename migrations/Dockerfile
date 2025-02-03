# Start with a base image that has Java installed
FROM openjdk:21-slim

RUN mkdir -p /app \
     -p /app/logs

# Copy the application JAR file into the container
COPY target/*.jar /app/app.jar

# Set the working directory inside the container
WORKDIR /app

# Expose the port your app runs on
EXPOSE 2000

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]