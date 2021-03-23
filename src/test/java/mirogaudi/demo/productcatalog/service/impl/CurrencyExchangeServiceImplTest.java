package mirogaudi.demo.productcatalog.service.impl;

import mirogaudi.demo.productcatalog.connector.ConnectorRuntimeException;
import mirogaudi.demo.productcatalog.connector.CurrencyExchangeRatesServiceConnector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Currency;

import static java.math.BigDecimal.ONE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyExchangeServiceImplTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency CNY = Currency.getInstance("CNY");

    @Mock
    private CurrencyExchangeRatesServiceConnector currencyExchangeRatesServiceConnector;

    @InjectMocks
    private CurrencyExchangeServiceImpl sut;

    @ParameterizedTest
    @NullSource
    void convert_null_amount(BigDecimal amount) {
        assertThrows(IllegalArgumentException.class,
                () -> sut.convert(amount, EUR, USD));
    }

    @ParameterizedTest
    @NullSource
    void convert_null_fromCurrency(Currency fromCurrency) {
        assertThrows(IllegalArgumentException.class,
                () -> sut.convert(ONE, fromCurrency, USD));
    }

    @ParameterizedTest
    @NullSource
    void convert_null_toCurrency(Currency toCurrency) {
        assertThrows(IllegalArgumentException.class,
                () -> sut.convert(ONE, EUR, toCurrency));
    }

    @Test
    void convert_same_currency() {
        var amount = BigDecimal.valueOf(100.00);
        Currency currency = EUR;

        StepVerifier.create(sut.convert(amount, currency, currency))
                .expectSubscription()
                .expectNextMatches(convertedAmount -> amount.compareTo(convertedAmount) == 0)
                .verifyComplete();

        verifyNoInteractions(currencyExchangeRatesServiceConnector);
    }

    @Test
    void convert_ok() {
        when(currencyExchangeRatesServiceConnector.getCachedCurrencyExchangeRate(USD, EUR))
                .thenReturn(BigDecimal.valueOf(0.83382));

        StepVerifier.create(sut.convert(BigDecimal.valueOf(100.00), USD, EUR))
                .expectSubscription()
                .expectNextMatches(convertedAmount -> BigDecimal.valueOf(83.382).compareTo(convertedAmount) == 0)
                .verifyComplete();

        verify(currencyExchangeRatesServiceConnector).getCachedCurrencyExchangeRate(USD, EUR);
        verify(currencyExchangeRatesServiceConnector, never()).getCurrencyExchangeRate(any(Currency.class), any(Currency.class));
    }

    @Test
    void convert_not_ok() {
        when(currencyExchangeRatesServiceConnector.getCachedCurrencyExchangeRate(any(Currency.class), any(Currency.class)))
                .thenThrow(new ConnectorRuntimeException("ConnectorRuntimeException", new Exception()));

        StepVerifier.create(sut.convert(ONE, CNY, EUR))
                .verifyError(ConnectorRuntimeException.class);

        verify(currencyExchangeRatesServiceConnector).getCachedCurrencyExchangeRate(CNY, EUR);
        verify(currencyExchangeRatesServiceConnector, never()).getCurrencyExchangeRate(any(Currency.class), any(Currency.class));
    }

}