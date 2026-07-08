package mirogaudi.productcatalog;

import io.swagger.v3.oas.models.OpenAPI;
import mirogaudi.productcatalog.client.FrankfurterRatesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
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

        // http service client config
        assertNotNull(context.getBean(FrankfurterRatesService.class));
        // cache config
        assertNotNull(context.getBean(CacheManager.class));
        // circuit breaker config
        assertNotNull(context.getBean(CircuitBreakerFactory.class));
        // open api config
        assertNotNull(context.getBean(OpenAPI.class));
    }

}
