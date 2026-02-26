# Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# copy pom and download dependecies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# copy source code
COPY src ./src
# cope generated code from jOOQ
COPY target/generated-sources/jooq ./target/generated-sources/jooq

# Create fat JAR, skipping test, jooq, flyway migrations
RUN mvn package -DskipTests -Djooq.codegen.skip=true -Dflyway.skip=true

# run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy only jar from build stage
COPY --from=build /app/target/kyc-aml-app-0.0.1-jar-with-dependencies.jar app.jar

# open ports for ktor
EXPOSE 8080

# Команда запуска
ENTRYPOINT ["java", "-jar", "app.jar"]