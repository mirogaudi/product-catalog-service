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

@SpringBootTest(classes = {ProductCatalogServiceApplication.class},
    properties = {"spring.http.serviceclient.rates.base-url=https://invalid.url"})
class FrankfurterRatesServiceConnectorCircuitBreakerIntegrationTest {

    @Autowired
    private FrankfurterRatesServiceConnector ratesServiceConnector;

    @Test
    void getCurrencyExchangeRate() {
        ConnectorRuntimeException exception = assertThrows(ConnectorRuntimeException.class,
            () -> ratesServiceConnector.getCurrencyExchangeRate(USD, EUR));
        assertEquals("Circuit breaker fallback called obtaining exchange rate (USD -> EUR) from rates service",
            exception.getMessage());
    }

}
