package mirogaudi.productcatalog.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Product Catalog API")
                .description("Demo product catalog service")
                .version("v1.0.0"))
            .externalDocs(new ExternalDocumentation()
                .description("Source code on GitHub")
                .url("https://github.com/mirogaudi/product-catalog-service"));
    }

}
