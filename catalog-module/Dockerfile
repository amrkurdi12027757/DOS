FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml .

COPY catalog-module/pom.xml   catalog-module/pom.xml
COPY gateway-module/pom.xml   gateway-module/pom.xml
COPY order-module/pom.xml     order-module/pom.xml
COPY shared/pom.xml           shared/pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl catalog-module -am -B dependency:go-offline

COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl catalog-module -am -DskipTests -T1C package -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY ../books.csv             books.csv
COPY --from=builder /build/catalog-module/target/catalog-1.0.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
