package mirogaudi.demo.productcatalog.config;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    public static final String CURRENCY_EXCHANGE_RATES = "currencyExchangeRates";

    @Scheduled(
            cron = "${currency.exchange.rates.cache.evict.cron}",
            zone = "${currency.exchange.rates.cache.evict.zone}"
    )
    @CacheEvict(value = CURRENCY_EXCHANGE_RATES, allEntries = true)
    public void evictCurrencyExchangeRatesCache() {
        // do nothing
    }

}
