FROM openjdk:25-rc-jdk
LABEL authors="riyqe"
WORKDIR /app
COPY target/ziovpo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]