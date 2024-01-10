# syntax=docker/dockerfile:1

FROM bellsoft/liberica-openjdk-alpine:21
LABEL org.opencontainers.image.authors="mirogaudi" \
    org.opencontainers.image.url="https://github.com/mirogaudi/product-catalog-service"
VOLUME /tmp
COPY target/product-catalog-service-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
