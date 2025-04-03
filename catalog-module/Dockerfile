FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

COPY . .

RUN mvn -pl catalog-module -am package

CMD ["java", "-jar", "catalog-module/target/catalog-1.0.jar"]
