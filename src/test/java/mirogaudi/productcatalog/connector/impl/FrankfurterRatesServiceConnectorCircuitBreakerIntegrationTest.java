package mirogaudi.productcatalog.connector.impl;

import mirogaudi.productcatalog.ProductCatalogServiceApplication;
import mirogaudi.productcatalog.connector.ConnectorRuntimeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static mirogaudi.productcatalog.testhelper.Currencies.EUR;
import static mirogaudi.productcatalog.testhelper.Currencies.USD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {ProductCatalogServiceApplication.class}, properties = {"pcs.rates.service.url=https://invalid.url"})
class FrankfurterRatesServiceConnectorCircuitBreakerIntegrationTest {

    @Autowired
    private FrankfurterRatesServiceConnector ratesServiceConnector;

    @Test
    void getCurrencyExchangeRate_() {
        ConnectorRuntimeException exception = assertThrows(ConnectorRuntimeException.class,
            () -> ratesServiceConnector.getCurrencyExchangeRate(USD, EUR));
        assertEquals("Circuit breaker fallback called trying to obtain USD to EUR rate from Frankfurter rates service.",
            exception.getMessage());
    }

}
