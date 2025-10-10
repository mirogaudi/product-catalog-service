package mirogaudi.productcatalog.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseEntityTest {

    @Test
    void testHashCode() {
        BaseEntity entity = new BaseEntity();
        assertEquals(BaseEntity.class.hashCode(), entity.hashCode());
    }
}
