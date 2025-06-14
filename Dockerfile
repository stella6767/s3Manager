FROM openjdk:21
LABEL maintainer="stella6767"
WORKDIR /app
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar","-Dspring.profiles.active=prod","app.jar"]
