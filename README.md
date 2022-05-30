![Lines of code](https://img.shields.io/tokei/lines/github/mirogaudi/product-catalog-service)
![GitHub top language](https://img.shields.io/github/languages/top/mirogaudi/product-catalog-service)
![GitHub maven workflow status](https://img.shields.io/github/workflow/status/mirogaudi/product-catalog-service/Java_CI_with_Maven)
![JaCoCo coverage](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/mirogaudi/product-catalog-service/master/.github/badges/jacoco.json)
![JaCoCo branches](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/mirogaudi/product-catalog-service/master/.github/badges/branches.json)
![GitHub license](https://img.shields.io/github/license/mirogaudi/product-catalog-service)

# Product Catalog Service with REST API

## Description

Application is a demo of a product catalog having simplified logic.

### Used technologies

- Java 17
- Maven (wrapper)
- Spring Boot
- Spring Web MVC
- Spring Cache (Caffeine)
- Spring Data JPA
- Flyway DB migration tool
- H2 in-memory DB
- Project Reactor
- Resilience4j
- Lombok
- Docker
- OpenAPI 3 & Swagger UI (springdoc-openapi)
- JUnit Jupiter
- Mockito
- JaCoCo

#### Misc

- Repository is licensed under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)
- Repository uses GitHub Actions ([Java_CI_with_Maven](.github/workflows/maven.yml))
- Repository uses Dependabot version updates ([dependabot.yml](.github/dependabot.yml))
- Readme badges are rendered using [shields.io](https://github.com/badges/shields)
- JaCoCo badges are generated by [jacoco-badge-generator](https://github.com/cicirello/jacoco-badge-generator)
- ER diagram is generated by [mermaid.live](https://mermaid.live)
- ASCII Art for [SpringBoot banner](src/main/resources/banner.txt) was generated
  with [TAAG](http://patorjk.com/software/taag) (font Calvin S)

### Functionality

- Application implements CRUD operations for Categories and Products
- Category and Product services are `@Transactional` to avoid race condition
- Application gets currency exchange rates from Frankfurter [https://frankfurter.app](https://frankfurter.app)
    - Frankfurter REST API is called via Spring `RestTemplate` and decorated with Resilience4j `CircuitBreaker`
- Rates are cached with `Caffeine` during a period of their validity
    - Cache eviction is scheduled to `16:01 CET MON-FRI`

### Database

Application DB stores categorized products:

- Categories are multilevel *(i.e. `Computers` <-- `Portable computers` <-- `Tablets`)*
- Products can relate to multiple categories *(i.e. `MacBook` relates to `Notebook` and to `Apple`)*
- Product prices are stored in original and base currency *(i.e. original price in `USD` and calculated price
  in `EUR`)*

[![](https://mermaid.ink/img/pako:eNqtUs1ugzAMfhUr5_YFOG-79DJp10jITTyIBAkyySQEvPsCbVoYjF2Wm-3vxz_phXKaRCaIXwwWjLW08HgKPRWOO-iXWYCrKYz1YDS8X9aVL2RVIoPFmnY5DTJZnxu9rI7SLsOGnQ7K_4drqjieyFjlKnBsQHVrmA01sVFPWBOjX6SOFTbE_dnyP1abYHHYt8suIglsIT8cH07DAOczuOGZyUCKEtv7UaQ4ovWb1ie2ctajsW0qtmuRdMlDjStVzhYteJdMDc064iTiUms0On7QeU9S-JLijcXE0_SJoZrbHiM0NDqyX7XxjkXmOdBJYPDuo7MqxTfM_affkuM3QcjoYg)](https://mermaid.live/edit#pako:eNqtUs1ugzAMfhUr5_YFOG-79DJp10jITTyIBAkyySQEvPsCbVoYjF2Wm-3vxz_phXKaRCaIXwwWjLW08HgKPRWOO-iXWYCrKYz1YDS8X9aVL2RVIoPFmnY5DTJZnxu9rI7SLsOGnQ7K_4drqjieyFjlKnBsQHVrmA01sVFPWBOjX6SOFTbE_dnyP1abYHHYt8suIglsIT8cH07DAOczuOGZyUCKEtv7UaQ4ovWb1ie2ctajsW0qtmuRdMlDjStVzhYteJdMDc064iTiUms0On7QeU9S-JLijcXE0_SJoZrbHiM0NDqyX7XxjkXmOdBJYPDuo7MqxTfM_affkuM3QcjoYg)

DB migration is done with Flyway using scripts:

- [V1__create_schema.sql](src/main/resources/db/migration/V1__create_schema.sql)
    - initial script `target/classes/db/create_schema.sql` is generated with Spring Data JPA when starting
      application with `dev` profile (for details see [application-dev.yml](src/main/resources/application-dev.yml))
- [V2__insert_data.sql](src/main/resources/db/migration/V2__insert_data.sql)

Application uses H2 in-memory DB.

### Configuration

See configuration in [application.yml](src/main/resources/application.yml):

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

## Getting started

### Maven build

```shell
# Build with Maven wrapper
$ ./mvnw clean package
```

### Docker build

```shell
# Build and tag docker image with Docker (requires artifacts to be already built)
$ docker build -t mirogaudi/product-catalog-service:1.0.0 .
$ docker tag mirogaudi/product-catalog-service:1.0.0 mirogaudi/product-catalog-service:latest

# Build docker image with Maven wrapper
$ ./mvnw clean package -Pdocker
```

### Run

```shell
# Run with Java
$ java -jar target/product-catalog-service-1.0.0.jar

# Run with Docker
$ docker run -it -d --rm --name product-catalog-service -p 8080:8080 mirogaudi/product-catalog-service:latest
```

- Or just run `ProductCatalogServiceApplication` in an IDE

### View and try API

- OpenAPI docs: [http://localhost:8080/pcs/v3/api-docs](http://localhost:8080/pcs/v3/api-docs)
- Swagger UI: [http://localhost:8080/pcs/swagger-ui/index.html](http://localhost:8080/pcs/swagger-ui/index.html)

### View DB

- H2 console [http://localhost:8080/pcs/h2-console](http://localhost:8080/pcs/h2-console)
    - url: `jdbc:h2:mem:pcs`
    - username: `sa`
    - password: `<empty>`

## Code quality

### Code coverage

```shell
# Build with Maven wrapper generating JaCoCo code coverage report
$ ./mvnw clean package -Pcode-coverage-report
```

### Dependencies vulnerabilities

```shell
# Build with Maven wrapper generating OWASP dependency vulnerability report
$ ./mvnw clean package -Pdependency-vulnerability-report
```

## Maintenance

### Update dependencies

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

## TODO:

- use static code analysis (Detekt/Findbugs/PMD/Checkstyle)
- use Micrometer
- use Spring WebFlux
