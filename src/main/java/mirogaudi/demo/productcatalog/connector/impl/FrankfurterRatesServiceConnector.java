package mirogaudi.demo.productcatalog.connector.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import mirogaudi.demo.productcatalog.connector.ConnectorRuntimeException;
import mirogaudi.demo.productcatalog.connector.RatesServiceConnector;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Currency;
import java.util.Map;
import java.util.function.Supplier;

import static mirogaudi.demo.productcatalog.config.CacheConfig.RATES_CACHE_NAME;

/**
 * Connector for the Frankfurter currency exchange rates service (https://www.frankfurter.app)
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
@Slf4j
public class FrankfurterRatesServiceConnector implements RatesServiceConnector {

    private final Supplier<URI> serviceUri;
    private final RestTemplate restTemplate;

    @CircuitBreaker(name = "frankfurterRatesService", fallbackMethod = "fallbackConvert")
    @Cacheable(value = RATES_CACHE_NAME)
    @Override
    public BigDecimal getCurrencyExchangeRate(@NonNull Currency fromCurrency,
                                              @NonNull Currency toCurrency) {
        try {
            // see "https://www.frankfurter.app/docs/#latest"
            String url = UriComponentsBuilder.fromUri(serviceUri.get())
                    .path("latest")
                    .queryParam("from", fromCurrency.getCurrencyCode())
                    .queryParam("to", toCurrency.getCurrencyCode())
                    .toUriString();

            RatesWrapper response = restTemplate.getForObject(url, RatesWrapper.class);
            Assert.state(response != null, String.format(
                    "No API response obtained from URL: %s", url));

            Double rate = response.getRates().get(toCurrency);
            Assert.state(rate != null, String.format(
                    "No currency exchange rate obtained converting from %s to %s", fromCurrency, toCurrency));

            return BigDecimal.valueOf(rate);
        } catch (RestClientException e) {
            throw new ConnectorRuntimeException(String.format(
                    "Failed to obtain currency exchange rate from '%s' to '%s'.",
                    fromCurrency, toCurrency
            ), e);
        }
    }

    private BigDecimal fallbackConvert(Currency fromCurrency,
                                       Currency toCurrency,
                                       Throwable throwable) {
        var message = String.format(
                "CircuitBreaker fallback called attempting to get currency exchange rate from '%s' to '%s'.",
                fromCurrency, toCurrency
        );

        LOG.error(message);
        throw new ConnectorRuntimeException(message, throwable);
    }

    @Value
    @Builder
    public static class RatesWrapper {
        Map<Currency, Double> rates;
    }

}
