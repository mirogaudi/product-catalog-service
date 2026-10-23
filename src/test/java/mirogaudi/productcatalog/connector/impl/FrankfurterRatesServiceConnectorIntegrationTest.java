package mirogaudi.productcatalog.connector.impl;

import mirogaudi.productcatalog.ProductCatalogServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static mirogaudi.productcatalog.testhelper.Currencies.EUR;
import static mirogaudi.productcatalog.testhelper.Currencies.USD;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {ProductCatalogServiceApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FrankfurterRatesServiceConnectorIntegrationTest {

    @Autowired
    private FrankfurterRatesServiceConnector ratesServiceConnector;

    @Test
    void getExchangeRate_ok() {
        BigDecimal rate = ratesServiceConnector.getExchangeRate(USD, EUR);

        assertNotNull(rate);
        assertTrue(rate.compareTo(BigDecimal.ZERO) > 0, "Exchange rate should be positive");
    }

}
