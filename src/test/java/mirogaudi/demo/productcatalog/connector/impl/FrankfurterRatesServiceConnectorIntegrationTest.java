package mirogaudi.demo.productcatalog.connector.impl;

import mirogaudi.demo.productcatalog.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;

import java.math.BigDecimal;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static mirogaudi.demo.productcatalog.config.CacheConfig.RATES_CACHE_NAME;
import static mirogaudi.demo.productcatalog.testhelper.Currencies.EUR;
import static mirogaudi.demo.productcatalog.testhelper.Currencies.USD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {Application.class})
class FrankfurterRatesServiceConnectorIntegrationTest {

    @Autowired
    private FrankfurterRatesServiceConnector ratesServiceConnector;
    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        getCache().map(Cache::invalidate);
    }

    @Test
    void getCurrencyExchangeRate_Cacheable() {
        SimpleKey key = new SimpleKey(USD, EUR);

        assertTrue(getCache().isPresent());

        // rate is not in the cache before the call
        assertFalse(getRateFromCache(key).isPresent());

        BigDecimal rate = ratesServiceConnector.getCurrencyExchangeRate(USD, EUR);
        assertNotNull(rate);

        // rate is in the cache after the call
        Optional<BigDecimal> cachedRate = getRateFromCache(key);
        assertTrue(cachedRate.isPresent());
        assertEquals(rate, cachedRate.get());
    }

    private Optional<Cache> getCache() {
        return ofNullable(cacheManager.getCache(RATES_CACHE_NAME));
    }

    private Optional<BigDecimal> getRateFromCache(SimpleKey key) {
        return getCache().map(cache -> cache.get(key, BigDecimal.class));
    }

}