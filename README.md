![JaCoCo](https://img.shields.io/github/languages/top/mirogaudi/product-catalog-service)
![JaCoCo](https://img.shields.io/github/workflow/status/mirogaudi/product-catalog-service/Java%20CI%20with%20Maven)
![JaCoCo](./.github/badges/jacoco.svg)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Demo Product Catalog Service with REST API

### Used technologies

- Java 17
- Maven Wrapper
- Docker
- Spring Boot
- Spring Web MVC
- Spring Cache (Caffeine)
- Spring Data JPA
- Flyway DB migration tool
- H2 in-memory DB
- Project Reactor
- Resilience4j
- Lombok
- OpenAPI 3 & Swagger UI (springdoc-openapi)
- JUnit Jupiter
- Mockito
- JaCoCo

### Getting started

#### Maven build

```shell
# Build with Maven wrapper
$ ./mvnw clean package
```

#### Docker build

```shell
# Build docker image with Maven wrapper
$ ./mvnw clean deploy -Pdocker

# Build and tag docker image with Docker
$ docker build -t mirogaudi/product-catalog-service:1.0.0 .
$ docker tag mirogaudi/product-catalog-service:1.0.0 mirogaudi/product-catalog-service:latest
```

#### Run application

```shell
# Run with Java
$ java -jar target/product-catalog-service-1.0.0.jar
  
# Run with Docker
$ docker run -it -d --rm --name product-catalog-service -p 8080:8080 mirogaudi/product-catalog-service:latest
```

- Or just run `Application` in an IDE

#### Test / check application API

- Swagger UI [http://localhost:8080/pcs/swagger-ui/index.html](http://localhost:8080/pcs/swagger-ui/index.html)
- OpenAPI [http://localhost:8080/pcs/v3/api-docs](http://localhost:8080/pcs/v3/api-docs)

### Description

Application is a demo of a product catalog with a simplified logic.

#### Database

- Application DB stores categorized products
    - Categories are multilevel *(i.e. `Computers` <-- `Notebooks` <-- `Tablets`)*
    - Products can relate to multiple categories *(i.e. `MacBook` relates to `Notebook` and to `Apple`)*
    - Product prices are stored in original and base currency *(i.e. original price in `USD` and calculated price
      in `EUR`)*


- DB is initialized with Flyway
    - [V1__init_schema.sql](./src/main/resources/db/migration/V1__init_schema.sql) (DB schema)
    - [V2__insert_data.sql](./src/main/resources/db/migration/V2__insert_data.sql) (initial data)


- Application uses H2 in-memory DB
    - H2 console [http://localhost:8080/pcs/h2-console](http://localhost:8080/pcs/h2-console)
        - JDBC URL: `jdbc:h2:mem:test`
        - User Name: `test`
        - Password: `test`

#### Functionality

- Application implements CRUD operations for Categories and Products
- Category and Product services are @Transactional to avoid race condition
- Application gets currency exchange rates from Frankfurter [https://frankfurter.app](https://frankfurter.app)
    - Frankfurter REST API is called via Spring `RestTemplate` and decorated with Resilience4j `CircuitBreaker`
- Rates are cached with `Caffeine` during a period of their validity
    - Cache eviction is scheduled to `16:01 CET MON-FRI`

### Configuration

See configuration in [application.yml](./src/main/resources/application.yml):

```yaml
# Product-catalog-service (pcs)
pcs:
  # Base currency
  base-currency-code: EUR

  # Rates service
  rates:
    service.url: https://frankfurter.app
    cache.evict:
      cron: 0 1 16 * * MON-FRI
      zone: CET
```

### Code quality

Build with Maven wrapper generating JaCoCo code coverage report (`target/site/jacoco/index.html`)

```shell
$ ./mvnw clean package -Pcode-coverage-report
```

### Maintenance

```shell
# Update Maven wrapper
$ ./mvnw -N io.takari:maven:wrapper

# Check for Maven plugins updates
$ ./mvnw versions:display-plugin-updates

# Check for Maven parent updates
$ ./mvnw versions:display-parent-updates

# Check for Maven property-linked dependencies updates
$ ./mvnw versions:display-property-updates

# Check for Maven dependencies updates
$ ./mvnw versions:display-dependency-updates
```

### Misc

- Repository is licensed under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)
- ASCII-Art for SpringBoot banner.txt is generated with [patorjk.com](http://patorjk.com/software/taag) (font Calvin S)

### TODO:

- use Micrometer
- use Gradle
- use Spring WebFlux
