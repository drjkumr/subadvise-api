FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline
COPY src/ src/
RUN ./mvnw -B package -DskipTests -Dmaven.compiler.proc=full

FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build --chown=10001:0 /workspace/target/subadvise-api-0.0.1-SNAPSHOT.jar app.jar
USER 10001
EXPOSE 7071
ENTRYPOINT ["java", "-jar", "/app/app.jar"]