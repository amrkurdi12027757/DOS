FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

COPY . .

RUN mvn -pl order-module -am package

CMD ["java", "-jar", "order-module/target/order-1.0.jar"]
