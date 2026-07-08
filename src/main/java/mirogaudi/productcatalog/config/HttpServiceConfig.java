package mirogaudi.productcatalog.config;

import mirogaudi.productcatalog.client.FrankfurterRatesService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration(proxyBeanMethods = false)
@ImportHttpServices(group = "frankfurter-rates-service", types = {FrankfurterRatesService.class})
public class HttpServiceConfig {
}
