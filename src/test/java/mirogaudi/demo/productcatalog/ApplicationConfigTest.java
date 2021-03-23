package mirogaudi.demo.productcatalog;

import mirogaudi.demo.productcatalog.config.ApplicationConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Import(ApplicationConfig.class)
class ApplicationConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void verifyContext() {
        assertNotNull(context.getBean("baseCurrency"));
        assertNotNull(context.getBean("currencyExchangeServiceUri"));
        assertNotNull(context.getBean("restTemplate"));
    }

}
