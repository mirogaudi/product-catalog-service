package mirogaudi.productcatalog.connector.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mirogaudi.productcatalog.connector.ConnectorRuntimeException;
import mirogaudi.productcatalog.connector.RatesServiceConnector;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Currency;
import java.util.Map;
import java.util.function.Supplier;

import static mirogaudi.productcatalog.config.CacheConfig.RATES_CACHE_NAME;

/**
 * Connector for the Frankfurter currency exchange rates service (<a href="https://www.frankfurter.app">https://www.frankfurter.app</a>).
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
@Slf4j
public class FrankfurterRatesServiceConnector implements RatesServiceConnector {

    private final Supplier<URI> ratesServiceUri;
    private final RestTemplate restTemplate;

    @CircuitBreaker(name = "frankfurterRatesService", fallbackMethod = "fallbackGetCurrencyExchangeRate")
    @Cacheable(value = RATES_CACHE_NAME)
    @Override
    public BigDecimal getCurrencyExchangeRate(@NonNull Currency fromCurrency,
                                              @NonNull Currency toCurrency) {
        try {
            // see "https://www.frankfurter.app/docs/#latest"
            String url = UriComponentsBuilder.fromUri(ratesServiceUri.get())
                .path("latest")
                .queryParam("from", fromCurrency.getCurrencyCode())
                .queryParam("to", toCurrency.getCurrencyCode())
                .toUriString();

            FrankfurterRates response = restTemplate.getForObject(url, FrankfurterRates.class);
            Assert.state(response != null, String.format(
                "No response obtained calling Frankfurter rates service API: %s", url));

            Double rate = response.rates().get(toCurrency);
            Assert.state(rate != null, String.format(
                "No %s to %s rate obtained calling Frankfurter rates service API.",
                fromCurrency, toCurrency));

            return BigDecimal.valueOf(rate);
        } catch (Exception e) {
            throw new ConnectorRuntimeException(String.format(
                "Failed to obtain %s to %s rate from Frankfurter rates service.",
                fromCurrency, toCurrency
            ), e);
        }
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod") // method is used by circuit breaker
    private BigDecimal fallbackGetCurrencyExchangeRate(Currency fromCurrency,
                                                       Currency toCurrency,
                                                       Throwable throwable) {
        throw new ConnectorRuntimeException(String.format(
            "Circuit breaker fallback called trying to obtain %s to %s rate from Frankfurter rates service.",
            fromCurrency, toCurrency
        ), throwable);
    }

    record FrankfurterRates(Currency base,
                            Map<Currency, Double> rates) {
    }

}
