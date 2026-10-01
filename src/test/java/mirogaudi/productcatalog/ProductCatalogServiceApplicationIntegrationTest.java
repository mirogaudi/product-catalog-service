package mirogaudi.productcatalog;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import mirogaudi.productcatalog.client.FrankfurterRatesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {ProductCatalogServiceApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProductCatalogServiceApplicationIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        // app config
        assertNotNull(context.getBean("baseCurrency"));

        // http services
        assertNotNull(context.getBean(FrankfurterRatesService.class));

        // cache
        assertNotNull(context.getBean(CacheManager.class));

        // resilience4j
        CircuitBreakerRegistry circuitBreakerRegistry = context.getBean(CircuitBreakerRegistry.class);
        assertNotNull(circuitBreakerRegistry);
        assertNotNull(circuitBreakerRegistry.circuitBreaker("cb-frankfurter-rates-service"));

        // swagger
        assertNotNull(context.getBean(OpenAPI.class));
    }

}
