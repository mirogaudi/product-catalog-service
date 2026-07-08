package mirogaudi.productcatalog.connector.impl;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import mirogaudi.productcatalog.ProductCatalogServiceApplication;
import mirogaudi.productcatalog.testhelper.Currencies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.stream.IntStream;

import static mirogaudi.productcatalog.config.CacheConfig.RATES_CACHE_NAME;
import static mirogaudi.productcatalog.testhelper.Currencies.EUR;
import static mirogaudi.productcatalog.testhelper.Currencies.USD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = {ProductCatalogServiceApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FrankfurterRatesServiceConnectorCacheIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private FrankfurterRatesServiceConnector ratesServiceConnector;

    @BeforeEach
    void setUp() {
        Optional.ofNullable(getRatesCache())
            .map(Cache::invalidate);
    }

    @ParameterizedTest
    @ValueSource(strings = {"USD", "CNY", "KRW", "JPY"})
    void getCurrencyExchangeRate_Cacheable(String fromCurrencyCode) {
        Currency fromCurrency = Currencies.getCurrency(fromCurrencyCode);
        Currency toCurrency = EUR;

        String key = fromCurrency.getCurrencyCode() + "-" + toCurrency.getCurrencyCode();

        Cache ratesCache = getRatesCache();
        assertNotNull(ratesCache);

        // rate is not in the cache before the call
        assertNull(ratesCache.get(key));

        BigDecimal rate = ratesServiceConnector.getExchangeRate(fromCurrency, toCurrency);
        assertNotNull(rate);

        // rate is in the cache after the call
        var cachedRate = ratesCache.get(key);
        assertNotNull(cachedRate);
        assertEquals(rate, cachedRate.get());
    }

    @Test
    void getCurrencyExchangeRate_CacheStats() {
        IntStream.range(0, 3).forEach(_ ->
            ratesServiceConnector.getExchangeRate(USD, EUR)
        );

        Cache ratesCache = getRatesCache();
        assertNotNull(ratesCache);

        CacheStats stats = ((CaffeineCache) ratesCache).getNativeCache().stats();
        assertEquals(1, stats.missCount());
        assertEquals(2, stats.hitCount());
    }

    private Cache getRatesCache() {
        return cacheManager.getCache(RATES_CACHE_NAME);
    }

}
