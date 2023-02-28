package mirogaudi.productcatalog.config;

import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;

@Configuration
public class CircuitBreakerConfig {

    @Bean
    public CircuitBreakerConfigCustomizer circuitBreakerCustomizer() {
        return CircuitBreakerConfigCustomizer
            .of("backendFrankfurter", builder -> builder
                .failureRateThreshold(50)
                .minimumNumberOfCalls(5)
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .permittedNumberOfCallsInHalfOpenState(3)
                .slidingWindowSize(10)
                .slidingWindowType(COUNT_BASED)
            );
    }
}
