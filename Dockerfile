FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/blog-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/storage/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
