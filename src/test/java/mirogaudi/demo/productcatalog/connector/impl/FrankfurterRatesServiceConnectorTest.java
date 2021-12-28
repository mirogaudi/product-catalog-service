package mirogaudi.demo.productcatalog.connector.impl;

import mirogaudi.demo.productcatalog.connector.ConnectorRuntimeException;
import mirogaudi.demo.productcatalog.connector.impl.FrankfurterRatesServiceConnector.RatesWrapper;
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

import static mirogaudi.demo.productcatalog.testhelper.Currencies.EUR;
import static mirogaudi.demo.productcatalog.testhelper.Currencies.USD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FrankfurterRatesServiceConnectorTest {

    private static final String SERVICE_URL = "https://service";
    private static final String SERVICE_PATH = SERVICE_URL + "/latest";

    @Mock
    private Supplier<URI> serviceUri;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FrankfurterRatesServiceConnector sut;

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
                .thenReturn(RatesWrapper.builder()
                        .rates(Maps.newHashMap(EUR, expectedRate))
                        .build()
                );

        BigDecimal actualRate = sut.getCurrencyExchangeRate(USD, EUR);
        assertEquals(expectedRate, actualRate.doubleValue());

        verify(restTemplate).getForObject(startsWith(SERVICE_PATH), eq(RatesWrapper.class));
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

        verify(restTemplate).getForObject(startsWith(SERVICE_PATH), eq(RatesWrapper.class));
        verify(serviceUri).get();
    }

}