package mirogaudi.productcatalog.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductTest {

    @Test
    void testHashCode() {
        assertEquals(Product.class.hashCode(), new Product().hashCode());
    }
}
