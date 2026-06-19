# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN addgroup --system delivery \
    && adduser --system --ingroup delivery delivery \
    && mkdir -p /app/uploads \
    && chown -R delivery:delivery /app/uploads

ENV JAVA_OPTS=""
ENV SERVER_PORT=8080

COPY --from=build /workspace/target/*.jar /app/delivery-backend.jar

USER delivery
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/delivery-backend.jar"]
