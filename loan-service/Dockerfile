FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
COPY config-server/pom.xml config-server/
COPY api-gateway/pom.xml api-gateway/
COPY book-service/pom.xml book-service/
COPY loan-service/pom.xml loan-service/

COPY loan-service/src loan-service/src

RUN mvn -q -pl loan-service -am package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

COPY --from=build /workspace/loan-service/target/loan-service-*.jar app.jar

EXPOSE 8092

ENTRYPOINT ["java", "-jar", "app.jar"]