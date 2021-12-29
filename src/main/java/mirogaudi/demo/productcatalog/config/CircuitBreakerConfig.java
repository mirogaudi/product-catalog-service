package mirogaudi.demo.productcatalog.config;

import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.TIME_BASED;

@Configuration
public class CircuitBreakerConfig {

    @Bean
    public CircuitBreakerConfigCustomizer circuitBreakerCustomizer() {
        return CircuitBreakerConfigCustomizer
                .of("backendFrankfurter", builder -> builder
                        .slidingWindowSize(10)
                        .permittedNumberOfCallsInHalfOpenState(3)
                        .slidingWindowType(TIME_BASED)
                        .minimumNumberOfCalls(20)
                        .waitDurationInOpenState(Duration.ofSeconds(10))
                        .failureRateThreshold(50)
                );
    }
}
