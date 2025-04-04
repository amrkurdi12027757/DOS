FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

COPY . .

RUN mvn -pl gateway-module -am package

CMD ["java", "-jar", "gateway-module/target/gateway-1.0.jar"]
