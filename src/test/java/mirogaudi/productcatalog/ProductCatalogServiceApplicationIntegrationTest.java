package mirogaudi.productcatalog;

import io.swagger.v3.oas.models.OpenAPI;
import mirogaudi.productcatalog.client.FrankfurterRatesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {ProductCatalogServiceApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductCatalogServiceApplicationIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void verifyContext() {
        assertNotNull(context.getBean("baseCurrency"));

        assertNotNull(context.getBean(CacheManager.class));
        assertNotNull(context.getBean(CircuitBreakerFactory.class));
        assertNotNull(context.getBean(FrankfurterRatesService.class));
        assertNotNull(context.getBean(OpenAPI.class));
    }

}
