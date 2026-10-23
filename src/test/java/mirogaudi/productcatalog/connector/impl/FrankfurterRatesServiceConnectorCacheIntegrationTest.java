package mirogaudi.productcatalog.connector.impl;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.github.tomakehurst.wiremock.WireMockServer;
import mirogaudi.productcatalog.ProductCatalogServiceApplication;
import mirogaudi.productcatalog.config.CacheConfig;
import mirogaudi.productcatalog.testhelper.Currencies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Currency;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static mirogaudi.productcatalog.config.CacheConfig.RATES_CACHE_NAME;
import static mirogaudi.productcatalog.testhelper.Currencies.EUR;
import static mirogaudi.productcatalog.testhelper.Currencies.USD;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {ProductCatalogServiceApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableWireMock({
    @ConfigureWireMock(name = "mock-frankfurter",
        baseUrlProperties = "spring.http.serviceclient.frankfurter.base-url")
})
class FrankfurterRatesServiceConnectorCacheIntegrationTest {

    @InjectWireMock("mock-frankfurter")
    static WireMockServer mockFrankfurterRatesService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private FrankfurterRatesServiceConnector ratesServiceConnector;

    @BeforeEach
    void setUp() {
        mockFrankfurterRatesService.resetAll();
        stubRates(USD, EUR, BigDecimal.ONE);

        Optional.ofNullable(getRatesCache())
            .map(Cache::invalidate);
    }

    @Test
    void getCurrencyExchangeRate_cached() {
        Cache ratesCache = getRatesCache();
        assertNotNull(ratesCache);

        // rate is not in the cache
        assertNull(ratesCache.get("USD-EUR"));

        // get rate
        BigDecimal rate = ratesServiceConnector.getExchangeRate(USD, EUR);
        assertNotNull(rate);

        // rate is in the cache
        var cachedRate = ratesCache.get("USD-EUR");
        assertNotNull(cachedRate);
        assertEquals(rate, cachedRate.get());
    }

    @Test
    void getCurrencyExchangeRate_cache_stats() {
        IntStream.range(0, 3).forEach(_ ->
            ratesServiceConnector.getExchangeRate(USD, EUR)
        );

        Cache ratesCache = getRatesCache();
        assertNotNull(ratesCache);

        CacheStats stats = ((CaffeineCache) ratesCache).getNativeCache().stats();
        assertEquals(1, stats.missCount());
        assertEquals(2, stats.hitCount());
    }

    @Nested
    class CacheEviction {

        @MockitoSpyBean
        private CacheConfig cacheConfig;

        @Test
        void evictRatesCache_clears_allEntries() {
            Currency cny = Currencies.getCurrency("CNY");
            Currency krw = Currencies.getCurrency("KRW");
            Currency jpy = Currencies.getCurrency("JPY");

            stubRates(cny, EUR, BigDecimal.ONE);
            stubRates(krw, EUR, BigDecimal.TWO);
            stubRates(jpy, EUR, BigDecimal.TEN);

            ratesServiceConnector.getExchangeRate(cny, EUR);
            ratesServiceConnector.getExchangeRate(krw, EUR);
            ratesServiceConnector.getExchangeRate(jpy, EUR);

            Cache ratesCache = getRatesCache();
            assertNotNull(ratesCache);

            assertNotNull(ratesCache.get("CNY-EUR"));
            assertNotNull(ratesCache.get("KRW-EUR"));
            assertNotNull(ratesCache.get("JPY-EUR"));

            cacheConfig.evictRatesCache();
            assertTrue(((CaffeineCache) ratesCache).getNativeCache().asMap().isEmpty());
        }

        @Nested
        class ScheduledCacheEviction {

            @DynamicPropertySource
            static void configureSchedulerProperties(DynamicPropertyRegistry registry) {
                registry.add("pcs.cache.rates-cache.evict.cron", () -> "*/2 * * * * *");
                registry.add("pcs.cache.rates-cache.evict.zone", () -> "UTC");
            }

            @Test
            void evictRatesCache_triggered_scheduled() {
                ratesServiceConnector.getExchangeRate(USD, EUR);

                Cache ratesCache = getRatesCache();
                assertNotNull(ratesCache);

                assertNotNull(ratesCache.get("USD-EUR"));

                await()
                    .atMost(Duration.ofSeconds(3))
                    .untilAsserted(() -> {
                        verify(cacheConfig).evictRatesCache();
                        assertTrue(((CaffeineCache) ratesCache).getNativeCache().asMap().isEmpty());
                    });
            }
        }
    }

    private static void stubRates(Currency fromCurrency, Currency toCurrency, BigDecimal rate) {
        mockFrankfurterRatesService.stubFor(get(urlPathEqualTo("/v2/rates"))
            .withQueryParam("base", equalTo(fromCurrency.getCurrencyCode()))
            .withQueryParam("quotes", equalTo(toCurrency.getCurrencyCode()))
            .willReturn(okJson("""
                [
                  {
                    "date": "2026-07-07",
                    "base": "%s",
                    "quote": "%s",
                    "rate": %s
                  }
                ]
                """.formatted(fromCurrency.getCurrencyCode(), toCurrency.getCurrencyCode(), rate.toPlainString())
            )));
    }

    private Cache getRatesCache() {
        return cacheManager.getCache(RATES_CACHE_NAME);
    }
}
