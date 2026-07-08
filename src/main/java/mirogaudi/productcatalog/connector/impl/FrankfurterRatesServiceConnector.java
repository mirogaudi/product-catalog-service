package mirogaudi.productcatalog.connector.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mirogaudi.productcatalog.client.FrankfurterRatesService;
import mirogaudi.productcatalog.client.FrankfurterRatesService.Rate;
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

    @Cacheable(
        value = RATES_CACHE_NAME,
        key = "#fromCurrency.currencyCode + '-' + #toCurrency.currencyCode"
    )
    @CircuitBreaker(name = "cb-frankfurter-rates-service", fallbackMethod = "getExchangeRateFallback")
    @Override
    public BigDecimal getExchangeRate(@NonNull Currency fromCurrency,
                                      @NonNull Currency toCurrency) {
        LOG.debug("Fetching exchange rate ({} -> {}) from rates service...", fromCurrency, toCurrency);

        Rate[] rates = ratesService.getRates(fromCurrency.getCurrencyCode(), toCurrency.getCurrencyCode());
        Assert.state(rates != null && rates.length > 0, String.format(
            "No exchange rate (%s -> %s) obtained from rates service", fromCurrency, toCurrency));
        Rate rate = rates[0];
        LOG.info("Obtained exchange rate ({} -> {}) from rates service: {}", fromCurrency, toCurrency, rate);

        return BigDecimal.valueOf(rate.rate());
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private BigDecimal getExchangeRateFallback(Currency fromCurrency,
                                               Currency toCurrency,
                                               IllegalStateException e) {
        String message = String.format(
            "CircuitBreaker: No exchange rate (%s -> %s) obtained from rates service", fromCurrency, toCurrency);
        LOG.error(message);
        throw new ConnectorRuntimeException(message, e);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private BigDecimal getExchangeRateFallback(Currency fromCurrency,
                                               Currency toCurrency,
                                               Throwable t) {
        String message = String.format(
            "CircuitBreaker: Failed to obtain exchange rate (%s -> %s) from rates service", fromCurrency, toCurrency);
        LOG.error(message);
        throw new ConnectorRuntimeException(message, t);
    }

}
