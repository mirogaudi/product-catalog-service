package mirogaudi.productcatalog.service.impl;

import mirogaudi.productcatalog.connector.ConnectorRuntimeException;
import mirogaudi.productcatalog.connector.RatesServiceConnector;
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
import static mirogaudi.productcatalog.testhelper.Currencies.EUR;
import static mirogaudi.productcatalog.testhelper.Currencies.USD;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyExchangeServiceImplTest {

    @Mock
    private RatesServiceConnector ratesServiceConnector;

    @InjectMocks
    private CurrencyExchangeServiceImpl sut;

    @ParameterizedTest
    @NullSource
    void convert_null_amount(BigDecimal amount) {
        assertThrows(IllegalArgumentException.class,
            () -> sut.convert(amount, USD, EUR).block());
    }

    @ParameterizedTest
    @NullSource
    void convert_null_fromCurrency(Currency fromCurrency) {
        assertThrows(IllegalArgumentException.class,
            () -> sut.convert(ONE, fromCurrency, EUR).block());
    }

    @ParameterizedTest
    @NullSource
    void convert_null_toCurrency(Currency toCurrency) {
        assertThrows(IllegalArgumentException.class,
            () -> sut.convert(ONE, USD, toCurrency).block());
    }

    @Test
    void convert_same_currency() {
        BigDecimal amount = BigDecimal.valueOf(100.00);
        Currency currency = EUR;

        StepVerifier.create(sut.convert(amount, currency, currency))
            .expectSubscription()
            .expectNextMatches(convertedAmount -> amount.compareTo(convertedAmount) == 0)
            .verifyComplete();

        verifyNoInteractions(ratesServiceConnector);
    }

    @Test
    void convert_ok() {
        BigDecimal amount = BigDecimal.valueOf(100.00);
        BigDecimal rate = BigDecimal.valueOf(0.83382);
        BigDecimal expectedConvertedAmount = BigDecimal.valueOf(83.382);

        when(ratesServiceConnector.getCurrencyExchangeRate(USD, EUR)).thenReturn(rate);

        StepVerifier.create(sut.convert(amount, USD, EUR))
            .expectSubscription()
            .expectNextMatches(convertedAmount -> expectedConvertedAmount.compareTo(convertedAmount) == 0)
            .verifyComplete();

        verify(ratesServiceConnector).getCurrencyExchangeRate(USD, EUR);
    }

    @Test
    void convert_not_ok() {
        when(ratesServiceConnector.getCurrencyExchangeRate(any(Currency.class), any(Currency.class)))
            .thenThrow(new ConnectorRuntimeException("ConnectorRuntimeException", new Exception()));

        StepVerifier.create(sut.convert(ONE, USD, EUR))
            .verifyError(ConnectorRuntimeException.class);

        verify(ratesServiceConnector).getCurrencyExchangeRate(USD, EUR);
    }

}
