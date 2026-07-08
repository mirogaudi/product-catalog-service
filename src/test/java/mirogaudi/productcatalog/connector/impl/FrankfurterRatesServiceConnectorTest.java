package mirogaudi.productcatalog.connector.impl;

import mirogaudi.productcatalog.client.FrankfurterRatesService;
import mirogaudi.productcatalog.client.FrankfurterRatesService.Rate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Currency;

import static mirogaudi.productcatalog.testhelper.Currencies.EUR;
import static mirogaudi.productcatalog.testhelper.Currencies.USD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FrankfurterRatesServiceConnectorTest {

    @Mock
    private FrankfurterRatesService ratesService;

    @InjectMocks
    private FrankfurterRatesServiceConnector sut;

    @ParameterizedTest
    @NullSource
    void getCurrencyExchangeRate_null_fromCurrency(Currency fromCurrency) {
        assertThrows(IllegalArgumentException.class,
            () -> sut.getExchangeRate(fromCurrency, EUR));
    }

    @ParameterizedTest
    @NullSource
    void getCurrencyExchangeRate_null_toCurrency(Currency toCurrency) {
        assertThrows(IllegalArgumentException.class,
            () -> sut.getExchangeRate(USD, toCurrency));
    }

    @Test
    void getCurrencyExchangeRate_ok() {
        double expectedRate = 0.89952d;
        Rate rate = new Rate(
            LocalDate.of(2026, Month.JULY, 2),
            USD.getCurrencyCode(),
            EUR.getCurrencyCode(),
            expectedRate
        );
        when(ratesService.getRates(anyString(), anyString()))
            .thenReturn(new Rate[]{rate});

        BigDecimal actualRate = sut.getExchangeRate(USD, EUR);
        assertEquals(expectedRate, actualRate.doubleValue());

        verify(ratesService).getRates(USD.getCurrencyCode(), EUR.getCurrencyCode());
    }

    @ParameterizedTest
    @NullSource
    void getCurrencyExchangeRate_not_ok_null(Rate[] response) {
        when(ratesService.getRates(anyString(), anyString()))
            .thenReturn(response);

        assertThrows(IllegalStateException.class,
            () -> sut.getExchangeRate(USD, EUR));

        verify(ratesService).getRates(USD.getCurrencyCode(), EUR.getCurrencyCode());
    }

    @ParameterizedTest
    @EmptySource
    void getCurrencyExchangeRate_not_ok_empty(Rate[] response) {
        when(ratesService.getRates(anyString(), anyString()))
            .thenReturn(response);

        assertThrows(IllegalStateException.class,
            () -> sut.getExchangeRate(USD, EUR));

        verify(ratesService).getRates(USD.getCurrencyCode(), EUR.getCurrencyCode());
    }

    @Test
    void getCurrencyExchangeRate_not_ok_RestClientException() {
        when(ratesService.getRates(anyString(), anyString()))
            .thenThrow(new RestClientException("error"));

        assertThrows(RestClientException.class,
            () -> sut.getExchangeRate(USD, EUR));

        verify(ratesService).getRates(USD.getCurrencyCode(), EUR.getCurrencyCode());
    }

}
