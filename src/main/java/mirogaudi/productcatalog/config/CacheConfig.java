package mirogaudi.productcatalog.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;

@Configuration
@EnableCaching
@EnableScheduling
@Slf4j
public class CacheConfig {

    public static final String RATES_CACHE_NAME = "rates";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(RATES_CACHE_NAME);
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofHours(24)) // safety fallback in case cron based eviction fails
            .maximumSize(500) // prevents unbounded growth
            .evictionListener((key, _, cause) ->
                LOG.info("Cache({}): key {} was evicted ({})", RATES_CACHE_NAME, key, cause))
            .removalListener((key, _, cause) ->
                LOG.info("Cache({}): key {} was removed ({})", RATES_CACHE_NAME, key, cause))
            .recordStats()
        );
        return cacheManager;
    }

    @Scheduled(
        cron = "${pcs.rates.cache.evict.cron}",
        zone = "${pcs.rates.cache.evict.zone}"
    )
    @CacheEvict(value = RATES_CACHE_NAME, allEntries = true)
    public void evictRatesCache() {
        // do nothing
    }

}
