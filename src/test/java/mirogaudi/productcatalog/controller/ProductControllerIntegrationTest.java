package mirogaudi.productcatalog.controller;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import mirogaudi.productcatalog.ProductCatalogServiceApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Rest Assured test based on initial test data, see src/main/resources/db/migration/V2__insert_data.sql
 * Uses an isolated in-memory H2 database to avoid polluting shared test data.
 */
@SpringBootTest(classes = {ProductCatalogServiceApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductControllerIntegrationTest {

    private static final String API_PRODUCTS = "/api/v1/products";

    private static final String DB_NAME = "pcs-test-products-" + System.currentTimeMillis();

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:" + DB_NAME);
    }

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port + "/pcs";
    }

    @Test
    void getProducts() {
        given()
            .when()
            .get(API_PRODUCTS)
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)));
    }

    @Test
    void getProduct_ok() {
        given()
            .when()
            .get(API_PRODUCTS + "/1")
            .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("name", equalTo("MacBook"))
            .body("originalPrice.toString()", equalTo("1999.99"))
            .body("originalCurrency", equalTo("USD"))
            .body("price.toString()", equalTo("1786.89"))
            .body("currency", equalTo("EUR"))
            .body("categoryIds", hasItems(2, 4));
    }

    @Test
    void getProduct_notFound() {
        var nonExistingProductId = -123L;

        given()
            .when()
            .get(API_PRODUCTS + "/" + nonExistingProductId)
            .then()
            .statusCode(404);
    }

    @Test
    void createProduct() {
        given()
            .when()
            .formParam("name", "Test Laptop")
            .formParam("originalPrice", "1299.99")
            .formParam("originalCurrency", "USD")
            .formParam("categoryId", 1, 4)
            .post(API_PRODUCTS)
            .then()
            .statusCode(201)
            .body("name", equalTo("Test Laptop"))
            .body("originalPrice.toString()", equalTo("1299.99"))
            .body("originalCurrency", equalTo("USD"))
            .body("price.toString()", notNullValue())
            .body("currency", equalTo("EUR"))
            .body("categoryIds", hasItems(1, 4));
    }

    @Test
    void createProduct_nameTooShort() {
        String tooShortName = "ab";

        given()
            .when()
            .formParam("name", tooShortName)
            .formParam("originalPrice", "99.99")
            .formParam("originalCurrency", "USD")
            .formParam("categoryId", 1)
            .post(API_PRODUCTS)
            .then()
            .statusCode(400);
    }

    @Test
    void createProduct_nonunique() {
        given()
            .when()
            .formParam("name", "iPad")
            .formParam("originalPrice", "99.99")
            .formParam("originalCurrency", "USD")
            .formParam("categoryId", 1)
            .post(API_PRODUCTS)
            .then()
            .statusCode(409);
    }

    @Test
    void updateProduct() {
        given()
            .when()
            .formParam("name", "ThinkPad1")
            .formParam("originalPrice", "1499.99")
            .formParam("originalCurrency", "EUR")
            .formParam("categoryId", 1, 5)
            .put(API_PRODUCTS + "/3")
            .then()
            .statusCode(200)
            .body("id", equalTo(3))
            .body("name", equalTo("ThinkPad1"))
            .body("originalPrice.toString()", equalTo("1499.99"))
            .body("originalCurrency", equalTo("EUR"))
            .body("price.toString()", equalTo("1499.99"))
            .body("currency", equalTo("EUR"))
            .body("categoryIds", hasItems(1, 5));
    }

    @Test
    void updateProduct_nameTooShort() {
        String tooShortName = "ab";

        given()
            .when()
            .formParam("name", tooShortName)
            .formParam("originalPrice", "99.99")
            .formParam("originalCurrency", "USD")
            .formParam("categoryId", 1)
            .put(API_PRODUCTS + "/1")
            .then()
            .statusCode(400);
    }

    @Test
    void updateProduct_nonexistent() {
        var nonExistingProductId = -123L;

        given()
            .when()
            .formParam("name", "Nonexistent")
            .formParam("originalPrice", "99.99")
            .formParam("originalCurrency", "USD")
            .formParam("categoryId", 1)
            .put(API_PRODUCTS + "/" + nonExistingProductId)
            .then()
            .statusCode(500);
    }

    @Test
    void deleteProduct() {
        Response response = given()
            .when()
            .formParam("name", "Tmp Product")
            .formParam("originalPrice", "1.00")
            .formParam("originalCurrency", "EUR")
            .formParam("categoryId", 1)
            .post(API_PRODUCTS);

        var tmpProductId = response.jsonPath().getLong("id");

        given()
            .when()
            .delete(API_PRODUCTS + "/" + tmpProductId)
            .then()
            .statusCode(200);
    }

    @Test
    void deleteProduct_nonexistent() {
        var nonExistingProductId = -123L;

        given()
            .when()
            .delete(API_PRODUCTS + "/" + nonExistingProductId)
            .then()
            .statusCode(500);
    }
}
