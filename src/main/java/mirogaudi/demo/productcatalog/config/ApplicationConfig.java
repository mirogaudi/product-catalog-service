package mirogaudi.demo.productcatalog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Currency;
import java.util.function.Supplier;

@Configuration
public class ApplicationConfig {

    @Value("${base.currency.code:EUR}")
    private String baseCurrencyCode;

    @Value("${currency.exchange.service.url}")
    private String currencyExchangeServiceUrl;

    @Bean
    public Supplier<Currency> baseCurrency() {
        return () -> Currency.getInstance(baseCurrencyCode);
    }

    @Bean
    public Supplier<URI> currencyExchangeServiceUri() {
        return () -> URI.create(currencyExchangeServiceUrl);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
