# syntax=docker/dockerfile:1

FROM eclipse-temurin:25.0.3_9-jdk
ARG APP_VERSION

LABEL org.opencontainers.image.authors="mirogaudi" \
    org.opencontainers.image.url="https://github.com/mirogaudi/product-catalog-service"

VOLUME /tmp
COPY target/product-catalog-service-$APP_VERSION.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
