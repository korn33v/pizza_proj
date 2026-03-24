# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src/ src/
RUN ./mvnw -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/pizza-delivery-0.1.0.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
