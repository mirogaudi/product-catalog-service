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
import static org.hamcrest.Matchers.hasSize;

/**
 * Rest Assured test based on initial test data, see src/main/resources/db/migration/V2__insert_data.sql
 * Uses an isolated in-memory H2 database to avoid polluting shared test data.
 */
@SpringBootTest(classes = {ProductCatalogServiceApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CategoryControllerIntegrationTest {

    private static final String API_CATEGORIES = "/api/v1/categories";

    private static final String DB_NAME = "pcs-test-categories-" + System.currentTimeMillis();

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
    void getCategories() {
        given()
            .when()
            .get(API_CATEGORIES)
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)));
    }

    @Test
    void getCategory_ok() {
        given()
            .when()
            .get(API_CATEGORIES + "/2")
            .then()
            .statusCode(200)
            .body("id", equalTo(2))
            .body("name", equalTo("Portable computers"))
            .body("parentId", equalTo(1));
    }

    @Test
    void getCategory_notFound() {
        var nonExistingCategoryId = -123L;

        given()
            .when()
            .get(API_CATEGORIES + "/" + nonExistingCategoryId)
            .then()
            .statusCode(404);
    }

    @Test
    void createCategory() {
        given()
            .when()
            .formParam("name", "Laptops")
            .formParam("parentId", 1)
            .post(API_CATEGORIES)
            .then()
            .statusCode(201)
            .body("name", equalTo("Laptops"))
            .body("parentId", equalTo(1));
    }

    @Test
    void createCategory_nameTooShort() {
        String tooShortName = "ab";

        given()
            .when()
            .formParam("name", tooShortName)
            .post(API_CATEGORIES)
            .then()
            .statusCode(400);
    }

    @Test
    void createCategory_nonunique() {
        given()
            .when()
            .formParam("name", "Computers")
            .post(API_CATEGORIES)
            .then()
            .statusCode(409);
    }

    @Test
    void updateCategory() {
        given()
            .when()
            .formParam("name", "Tablets1")
            .formParam("parentId", 1)
            .put(API_CATEGORIES + "/3")
            .then()
            .statusCode(200)
            .body("id", equalTo(3))
            .body("parentId", equalTo(1))
            .body("name", equalTo("Tablets1"));
    }

    @Test
    void updateCategory_remove_Parent() {
        given()
            .when()
            .formParam("name", "Tablets2")
            .put(API_CATEGORIES + "/3")
            .then()
            .statusCode(200)
            .body("id", equalTo(3))
            .body("parentId", equalTo(null))
            .body("name", equalTo("Tablets2"));
    }

    @Test
    void updateCategory_nameTooShort() {
        String tooShortName = "ab";

        given()
            .when()
            .formParam("name", tooShortName)
            .put(API_CATEGORIES + "/1")
            .then()
            .statusCode(400);
    }

    @Test
    void updateCategory_nonexistent() {
        var nonExistingCategoryId = -123L;

        given()
            .when()
            .formParam("name", "Nonexistent")
            .put(API_CATEGORIES + "/" + nonExistingCategoryId)
            .then()
            .statusCode(500);
    }

    @Test
    void deleteCategory() {
        Response response = given()
            .when()
            .formParam("name", "Tmp Category")
            .post(API_CATEGORIES);

        var tmpCategoryId = response.jsonPath().getLong("id");

        given()
            .when()
            .delete(API_CATEGORIES + "/" + tmpCategoryId)
            .then()
            .statusCode(200);
    }

    @Test
    void deleteCategory_nonexistent() {
        var nonExistingCategoryId = -123L;

        given()
            .when()
            .delete(API_CATEGORIES + "/" + nonExistingCategoryId)
            .then()
            .statusCode(500);
    }
}
