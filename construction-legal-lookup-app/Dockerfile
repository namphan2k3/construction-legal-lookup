FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["sh","-c","java \
-Xms64m \
-Xmx192m \
-XX:+UseSerialGC \
-XX:ActiveProcessorCount=1 \
-Dserver.port=$PORT \
-jar /app/app.jar"]