package mirogaudi.demo.productcatalog.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    public static final String RATES_CACHE_NAME = "rates";

    @Bean
    public CacheManager cacheManager() {
        return new CaffeineCacheManager(RATES_CACHE_NAME);
    }

    @Scheduled(
            cron = "${rates.cache.evict.cron}",
            zone = "${rates.cache.evict.zone}"
    )
    @CacheEvict(value = RATES_CACHE_NAME, allEntries = true)
    public void evictRatesCache() {
        // do nothing
    }

}
