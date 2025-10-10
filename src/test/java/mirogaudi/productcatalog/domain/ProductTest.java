package mirogaudi.productcatalog.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductTest {

    @Test
    void testHashCode() {
        Product entity = new Product();
        String name = "name";
        entity.setName(name);

        assertEquals(name.hashCode(), entity.hashCode());
    }
}
