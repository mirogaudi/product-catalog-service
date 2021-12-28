package mirogaudi.demo.productcatalog;

import io.swagger.v3.oas.models.OpenAPI;
import mirogaudi.demo.productcatalog.config.ApplicationConfig;
import mirogaudi.demo.productcatalog.config.CacheConfig;
import mirogaudi.demo.productcatalog.config.CircuitBreakerConfig;
import mirogaudi.demo.productcatalog.config.SwaggerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        ApplicationConfig.class,
        CacheConfig.class,
        CircuitBreakerConfig.class,
        SwaggerConfig.class
})
class ApplicationConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void verifyContext() {
        assertNotNull(context.getBean("baseCurrency"));
        assertNotNull(context.getBean("ratesServiceUri"));
        assertNotNull(context.getBean("restTemplate"));
        assertNotNull(context.getBean("ratesServiceConnector"));

        assertNotNull(context.getBean("cacheManager"));

        assertNotNull(context.getBean("defaultCustomizer"));
        assertNotNull(context.getBean("specificCustomizer"));

        assertNotNull(context.getBean(OpenAPI.class));
    }

}
