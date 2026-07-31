FROM maven:3.9.16-eclipse-temurin-25 AS builder
WORKDIR /build
COPY pom.xml .
COPY libs/pom.xml libs/
COPY libs/spring-web-lib/pom.xml libs/spring-web-lib/
COPY libs/standard-lib/pom.xml libs/standard-lib/
COPY domain/pom.xml domain/
COPY application/pom.xml application/
RUN mvn dependency:go-offline
COPY libs ./libs
COPY domain ./domain
COPY application ./application
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /build/application/target/application-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]