package mirogaudi.demo.productcatalog.connector.impl;

import mirogaudi.demo.productcatalog.config.ApplicationConfig;
import mirogaudi.demo.productcatalog.config.CacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Currency;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        ApplicationConfig.class,
        CacheConfig.class
})
class FrankfurterRatesServiceConnectorImplIntegrationTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Autowired
    private Supplier<URI> currencyExchangeServiceUri;
    @Autowired
    private RestTemplate restTemplate;

    private FrankfurterRatesServiceConnectorImpl sut;

    @BeforeEach
    void setUp() {
        sut = new FrankfurterRatesServiceConnectorImpl(currencyExchangeServiceUri, restTemplate);
    }

    @Test
    void getCurrencyExchangeRate() {
        assertNotNull(sut.getCurrencyExchangeRate(USD, EUR));
    }
}