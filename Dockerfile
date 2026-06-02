FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/webshop-0.0.1-SNAPSHOT.war app.war
EXPOSE 8081
ENTRYPOINT ["java", "-Dspring.profiles.active=render", "-jar", "app.war"]
