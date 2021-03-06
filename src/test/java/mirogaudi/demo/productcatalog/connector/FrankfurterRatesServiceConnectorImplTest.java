package mirogaudi.demo.productcatalog.connector;

import mirogaudi.demo.productcatalog.connector.impl.FrankfurterRatesServiceConnectorImpl;
import mirogaudi.demo.productcatalog.connector.impl.FrankfurterRatesServiceConnectorImpl.FrankfurterCurrencyExchangeRates;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Currency;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FrankfurterRatesServiceConnectorImplTest {

    private static final String SERVICE_URL = "https://service";
    private static final String SERVICE_PATH = SERVICE_URL + "/latest";

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Mock
    private Supplier<URI> serviceUri;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FrankfurterRatesServiceConnectorImpl sut;

    @ParameterizedTest
    @NullSource
    void getCurrencyExchangeRate_null_fromCurrency(Currency fromCurrency) {
        assertThrows(IllegalArgumentException.class,
                () -> sut.getCurrencyExchangeRate(fromCurrency, EUR));
    }

    @ParameterizedTest
    @NullSource
    void getCurrencyExchangeRate_null_toCurrency(Currency toCurrency) {
        assertThrows(IllegalArgumentException.class,
                () -> sut.getCurrencyExchangeRate(USD, toCurrency));
    }

    @Test
    void getCurrencyExchangeRate_ok() {
        when(serviceUri.get()).thenReturn(URI.create(SERVICE_URL));

        double expectedRate = Double.parseDouble("0.89952");
        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn(FrankfurterCurrencyExchangeRates.builder()
                        .rates(Maps.newHashMap(EUR, expectedRate))
                        .build()
                );

        BigDecimal actualRate = sut.getCurrencyExchangeRate(USD, EUR);
        assertEquals(expectedRate, actualRate.doubleValue());

        verify(restTemplate).getForObject(startsWith(SERVICE_PATH), eq(FrankfurterCurrencyExchangeRates.class));
        verify(serviceUri).get();
    }

    @Test
    void getCurrencyExchangeRate_not_ok_RestClientException() {
        when(serviceUri.get()).thenReturn(URI.create(SERVICE_URL));

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new RestClientException("error"));

        assertThrows(ConnectorRuntimeException.class, () ->
                sut.getCurrencyExchangeRate(USD, EUR)
        );

        verify(restTemplate).getForObject(startsWith(SERVICE_PATH), eq(FrankfurterCurrencyExchangeRates.class));
        verify(serviceUri).get();
    }

}