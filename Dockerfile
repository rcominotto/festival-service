# ---- Build stage (Maven + JDK 17) ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache-friendly: eerst alleen pom.xml, dan dependencies, dan source
COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

# ---- Runtime stage (JRE 17) ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built Spring Boot jar
COPY --from=build /app/target/*.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]




