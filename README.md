![JaCoCo](https://img.shields.io/github/languages/top/mirogaudi/product-catalog-service)
![JaCoCo](https://img.shields.io/github/workflow/status/mirogaudi/product-catalog-service/Java%20CI%20with%20Maven)
![JaCoCo](./.github/badges/jacoco.svg)

## Demo Product Catalog Service with REST API

### Used technologies

- Java 11
- Maven Wrapper
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

### Getting started

- Build with Maven Wrapper
  ```shell
  $ ./mvnw clean package
  ```
- Generate code coverage report
  ```shell
  $ ./mvnw clean package -Pcode-coverage-report
  ```
- Start application
  ```shell
  $ java -jar target/product-catalog-service-1.0.0.jar
  ```
    - Or run `mirogaudi.demo.productcatalog.Application` in an IDE


- Test application or check API with:
    - Swagger UI [http://localhost:8080/pcs/swagger-ui/index.html](http://localhost:8080/pcs/swagger-ui/index.html)
    - OpenAPI [http://localhost:8080/pcs/v3/api-docs](http://localhost:8080/pcs/v3/api-docs)

### Description

- Application is a demo of a categorized product catalog with simplified logic and DB schema
    - Categories are multilevel *(i.e Computers <- Notebooks <- Tablets)*
    - Product can relate to multiple categories *(i.e MacBook relates to Notebooks and Apple)*
    - Product prices are stored in original and base currency *(i.e. original price in USD and calculated price in EUR)*


- Application functionality:
    - CRUD operations for Categories
    - CRUD operations for Products
    - Category and Product services are @Transactional to avoid race condition
    - Application gets currency exchange rates from Frankfurter [https://frankfurter.app](https://frankfurter.app)
        - Rates are cached with Caffeine during a period of their validity
        - Cache eviction is scheduled to `16:01 CET MON-FRI`  
          *(Since Frankfurter refresh rates around 16:00 CET every working day)*
        - Frankfurter REST API call is decorated with circuit breaker (Resilience4j) and uses Spring RestTemplate


- Application uses an in-memory H2 DB
    - DB is initialized with Flyway
        - DB schema [V1__init_schema.sql](./src/main/resources/db/migration/V1__init_schema.sql)
        - Initial data [V2__insert_data.sql](./src/main/resources/db/migration/V2__insert_data.sql)
    - H2 console [http://localhost:8080/pcs/h2-console](http://localhost:8080/pcs/h2-console)
        - JDBC URL: `jdbc:h2:mem:test`
        - User Name: `test`
        - Password: `test`

### Configuration

For details see [application.yml](./src/main/resources/application.yml)

- Base currency
  ```properties
  pcs.base-currency-code=EUR
  ```
- Currency exchange rates service
  ```properties
  pcs.rates.service.url=https://frankfurter.app
  ```

### Maintenance

- Update maven wrapper

```shell
  $ mvn -N io.takari:maven:wrapper
```

- Check for maven plugins updates

```shell
  $ ./mvnw versions:display-plugin-updates
```

- Check for maven parent updates

```shell
  $ ./mvnw versions:display-parent-updates
```

- Check for maven property-linked dependencies updates

```shell
  $ ./mvnw versions:display-property-updates
```

- Check for maven dependencies updates

```shell
  $ ./mvnw versions:display-dependency-updates
```

### Misc

- ASCII-Art for SpringBoot banner.txt was generated with [patorjk.com](http://patorjk.com/software/taag) (font Calvin S)

### TODOs:

- use application.yml
- use Java 17
- use Spring WebFlux
- use Docker
