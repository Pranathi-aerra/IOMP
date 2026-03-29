# Step 1: Use Maven to build the application
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Use JDK to run the application
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Install Google Chrome for Selenium
RUN apt-get update && apt-get install -y wget gnupg
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add -
RUN sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list'
RUN apt-get update && apt-get install -y google-chrome-stable

# Copy the built jar from the first step
COPY --from=build /app/target/*.jar app.jar

# Expose the port Render gives us
EXPOSE 8081

ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]