package mirogaudi.demo.productcatalog;

import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {Application.class})
class ApplicationConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void verifyContext() {
        assertNotNull(context.getBean("baseCurrency"));
        assertNotNull(context.getBean("ratesServiceUri"));
        assertNotNull(context.getBean("restTemplate"));

        assertNotNull(context.getBean(CacheManager.class));
        assertNotNull(context.getBean(CircuitBreakerConfigCustomizer.class));
        assertNotNull(context.getBean(OpenAPI.class));
    }

}
