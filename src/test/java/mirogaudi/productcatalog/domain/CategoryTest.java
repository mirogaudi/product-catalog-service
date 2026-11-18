package mirogaudi.productcatalog.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoryTest {

    @Test
    void testHashCode() {
        assertEquals(Category.class.hashCode(), new Category().hashCode());
    }
}
