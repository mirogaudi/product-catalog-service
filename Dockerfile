# syntax=docker/dockerfile:1

FROM eclipse-temurin:21.0.6_7-jdk
LABEL org.opencontainers.image.authors="mirogaudi" \
    org.opencontainers.image.url="https://github.com/mirogaudi/product-catalog-service"
VOLUME /tmp
COPY target/product-catalog-service-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
