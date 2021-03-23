package mirogaudi.demo.productcatalog.connector.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import mirogaudi.demo.productcatalog.config.CacheConfig;
import mirogaudi.demo.productcatalog.connector.ConnectorRuntimeException;
import mirogaudi.demo.productcatalog.connector.CurrencyExchangeRatesServiceConnector;
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

/**
 * Connector for the Frankfurter currency data API (https://www.frankfurter.app)
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
@Slf4j
public class FrankfurterRatesServiceConnectorImpl implements CurrencyExchangeRatesServiceConnector {

    private final Supplier<URI> currencyExchangeServiceUri;
    private final RestTemplate restTemplate;

    @Cacheable(cacheNames = CacheConfig.CURRENCY_EXCHANGE_RATES)
    @Override
    public BigDecimal getCachedCurrencyExchangeRate(@NonNull Currency fromCurrency,
                                                    @NonNull Currency toCurrency) {
        return getCurrencyExchangeRate(fromCurrency, toCurrency);
    }

    @CircuitBreaker(name = "frankfurterRatesService", fallbackMethod = "fallbackConvert")
    @Override
    public BigDecimal getCurrencyExchangeRate(@NonNull Currency fromCurrency,
                                              @NonNull Currency toCurrency) {
        try {
            // see "https://www.frankfurter.app/docs/#latest"
            String url = UriComponentsBuilder.fromUri(currencyExchangeServiceUri.get())
                    .path("latest")
                    .queryParam("from", fromCurrency.getCurrencyCode())
                    .queryParam("to", toCurrency.getCurrencyCode())
                    .toUriString();

            FrankfurterCurrencyExchangeRates response = restTemplate.getForObject(url, FrankfurterCurrencyExchangeRates.class);
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
    public static class FrankfurterCurrencyExchangeRates {
        Map<Currency, Double> rates;
    }

}
