package mirogaudi.productcatalog.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoryTest {

    @Test
    void testHashCode() {
        Category entity = new Category();
        String name = "name";
        entity.setName(name);

        assertEquals(name.hashCode(), entity.hashCode());
    }
}
