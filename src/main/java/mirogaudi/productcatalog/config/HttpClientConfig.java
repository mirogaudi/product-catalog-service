package mirogaudi.productcatalog.config;

import mirogaudi.productcatalog.client.FrankfurterRatesService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration(proxyBeanMethods = false)
@ImportHttpServices(
    group = "frankfurter", types = {FrankfurterRatesService.class},
    clientType = HttpServiceGroup.ClientType.REST_CLIENT)
public class HttpClientConfig {
}
