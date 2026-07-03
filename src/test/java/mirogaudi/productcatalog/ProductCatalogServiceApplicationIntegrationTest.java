package mirogaudi.productcatalog;

import io.swagger.v3.oas.models.OpenAPI;
import mirogaudi.productcatalog.client.FrankfurterRatesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {ProductCatalogServiceApplication.class})
class ProductCatalogServiceApplicationIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void verifyContext() {
        assertNotNull(context.getBean("baseCurrency"));

        assertNotNull(context.getBean(CacheManager.class));
        assertNotNull(context.getBean(Resilience4JCircuitBreakerFactory.class));
        assertNotNull(context.getBean(FrankfurterRatesService.class));
        assertNotNull(context.getBean(OpenAPI.class));
    }

}
