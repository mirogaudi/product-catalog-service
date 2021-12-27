package mirogaudi.demo.productcatalog.config;

import mirogaudi.demo.productcatalog.connector.RatesServiceConnector;
import mirogaudi.demo.productcatalog.connector.impl.FrankfurterRatesServiceConnector;
import mirogaudi.demo.productcatalog.service.CurrencyExchangeService;
import mirogaudi.demo.productcatalog.service.impl.CurrencyExchangeServiceImpl;
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

    @Value("${rates.service.url}")
    private String ratesServiceUrl;

    @Bean
    public Supplier<Currency> baseCurrency() {
        return () -> Currency.getInstance(baseCurrencyCode);
    }

    @Bean
    public Supplier<URI> ratesServiceUri() {
        return () -> URI.create(ratesServiceUrl);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RatesServiceConnector ratesServiceConnector(Supplier<URI> ratesServiceUri,
                                                       RestTemplate restTemplate) {
        return new FrankfurterRatesServiceConnector(ratesServiceUri, restTemplate);
    }

    @Bean
    public CurrencyExchangeService currencyExchangeService(RatesServiceConnector ratesServiceConnector) {
        return new CurrencyExchangeServiceImpl(ratesServiceConnector);
    }

}
