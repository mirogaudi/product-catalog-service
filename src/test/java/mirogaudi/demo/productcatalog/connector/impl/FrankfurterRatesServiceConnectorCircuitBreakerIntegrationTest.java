package mirogaudi.demo.productcatalog.connector.impl;

import mirogaudi.demo.productcatalog.Application;
import mirogaudi.demo.productcatalog.connector.ConnectorRuntimeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static mirogaudi.demo.productcatalog.testhelper.Currencies.EUR;
import static mirogaudi.demo.productcatalog.testhelper.Currencies.USD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {Application.class}, properties = {"pcs.rates.service.url=https://invalid.url"})
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