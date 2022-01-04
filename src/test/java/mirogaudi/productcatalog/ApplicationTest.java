package mirogaudi.productcatalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ApplicationTest {

    @Test
    void main() {
        assertDoesNotThrow(() -> Application.main(new String[]{}));
    }
}