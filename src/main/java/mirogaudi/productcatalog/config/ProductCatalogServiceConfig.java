package mirogaudi.productcatalog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Currency;
import java.util.function.Supplier;

@Configuration
public class ProductCatalogServiceConfig {

    @Value("${pcs.base-currency-code:EUR}")
    private String baseCurrencyCode;

    @Bean
    public Supplier<Currency> baseCurrency() {
        return () -> Currency.getInstance(baseCurrencyCode);
    }

}
