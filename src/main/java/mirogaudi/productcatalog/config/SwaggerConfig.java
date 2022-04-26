package mirogaudi.productcatalog.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Product Catalog API")
                .description("Demo product catalog service")
                .version("v1.0.0")
                .contact(new Contact()
                    .email("mirogaudi@ya.ru")
                    .url("https://github.com/mirogaudi"))
                .license(new License()
                    .name("Apache-2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .externalDocs(new ExternalDocumentation()
                .description("Source code on GitHub")
                .url("https://github.com/mirogaudi/product-catalog-service"));
    }

}
