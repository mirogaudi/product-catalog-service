package mirogaudi.productcatalog.connector.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mirogaudi.productcatalog.client.FrankfurterRatesService;
import mirogaudi.productcatalog.connector.ConnectorRuntimeException;
import mirogaudi.productcatalog.connector.RatesServiceConnector;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Currency;

import static mirogaudi.productcatalog.config.CacheConfig.RATES_CACHE_NAME;

/**
 * Connector for the <a href="https://frankfurter.dev">frankfurter.dev</a> currency exchange rates service.
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
@Slf4j
public class FrankfurterRatesServiceConnector implements RatesServiceConnector {

    private final FrankfurterRatesService ratesService;

    @CircuitBreaker(name = "frankfurter-rates-service", fallbackMethod = "getCurrencyExchangeRateFallback")
    @Cacheable(
        value = RATES_CACHE_NAME,
        key = "#fromCurrency.currencyCode + '-' + #toCurrency.currencyCode"
    )
    @Override
    public BigDecimal getCurrencyExchangeRate(@NonNull Currency fromCurrency,
                                              @NonNull Currency toCurrency) {
        try {
            LOG.debug("Fetching exchange rate ({} -> {}) from rates service...", fromCurrency, toCurrency);

            FrankfurterRatesService.Rate[] rates = ratesService.getRates(fromCurrency.getCurrencyCode(), toCurrency.getCurrencyCode());
            Assert.state(rates != null && rates.length > 0, String.format(
                "No exchange rate (%s -> %s) obtained from rates service",
                fromCurrency, toCurrency
            ));

            FrankfurterRatesService.Rate rate = rates[0];
            LOG.info("Obtained exchange rate ({} -> {}) from rates service: {}", fromCurrency, toCurrency, rate);

            return BigDecimal.valueOf(rate.rate());
        } catch (Exception e) {
            LOG.error("Failed to obtain exchange rate ({} -> {}) from rates service", fromCurrency, toCurrency);
            throw new ConnectorRuntimeException(String.format(
                "Failed to obtain exchange rate (%s -> %s) from rates service",
                fromCurrency, toCurrency
            ), e);
        }
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod") // method is used by circuit breaker
    private BigDecimal getCurrencyExchangeRateFallback(Currency fromCurrency,
                                                       Currency toCurrency,
                                                       Throwable throwable) {
        throw new ConnectorRuntimeException(String.format(
            "Circuit breaker fallback called obtaining exchange rate (%s -> %s) from rates service",
            fromCurrency, toCurrency
        ), throwable);
    }

}
