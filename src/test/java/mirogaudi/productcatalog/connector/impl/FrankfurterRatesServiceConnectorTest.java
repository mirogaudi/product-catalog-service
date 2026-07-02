package mirogaudi.productcatalog.connector.impl;

import mirogaudi.productcatalog.client.FrankfurterRatesService;
import mirogaudi.productcatalog.connector.ConnectorRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
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
        double expectedRate = Double.parseDouble("0.89952");
        FrankfurterRatesService.Rate rate = new FrankfurterRatesService.Rate(
            LocalDate.of(2026, Month.JULY, 2),
            USD.getCurrencyCode(),
            EUR.getCurrencyCode(),
            expectedRate
        );
        when(ratesService.getRates(anyString(), anyString()))
            .thenReturn(new FrankfurterRatesService.Rate[]{rate});

        BigDecimal actualRate = sut.getCurrencyExchangeRate(USD, EUR);
        assertEquals(expectedRate, actualRate.doubleValue());

        verify(ratesService).getRates(USD.getCurrencyCode(), EUR.getCurrencyCode());
    }

    @Test
    void getCurrencyExchangeRate_not_ok_RestClientException() {
        when(ratesService.getRates(anyString(), anyString()))
            .thenThrow(new RestClientException("error"));

        assertThrows(ConnectorRuntimeException.class,
            () -> sut.getCurrencyExchangeRate(USD, EUR)
        );

        verify(ratesService).getRates(USD.getCurrencyCode(), EUR.getCurrencyCode());
    }

}
