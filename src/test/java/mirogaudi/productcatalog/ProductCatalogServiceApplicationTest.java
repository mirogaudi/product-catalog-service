package mirogaudi.productcatalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ProductCatalogServiceApplicationTest {

    @Test
    void main() {
        assertDoesNotThrow(() -> ProductCatalogServiceApplication.main(new String[]{}));
    }
}