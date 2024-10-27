FROM openjdk:17-jdk-alpine

WORKDIR /app
COPY build/libs/*.jar auction.jar

ENTRYPOINT ["java", "-jar", "auction.jar"]