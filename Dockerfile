FROM eclipse-temurin:21-jdk-alpine
WORKDIR /home/app
COPY build/docker/layers/libs /home/app/libs
COPY build/docker/layers/resources /home/app/resources
COPY build/docker/layers/application.jar /home/app/application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/home/app/application.jar"]
