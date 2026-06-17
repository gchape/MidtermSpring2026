FROM maven:3.9.8-eclipse-temurin-25 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=builder /build/target/uno.jar uno.jar

RUN mkdir -p logs

ENTRYPOINT ["java", "-jar", "uno.jar"]
CMD ["--bots", "3", "--games", "1"]