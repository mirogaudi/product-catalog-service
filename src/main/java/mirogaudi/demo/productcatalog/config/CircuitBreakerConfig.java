package mirogaudi.demo.productcatalog.config;

import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.ofDefaults;

@Configuration
public class CircuitBreakerConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(ofDefaults())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(4))
                        .build())
                .build());
    }

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> specificCustomizer() {
        return factory -> factory.configure(builder -> builder
                .circuitBreakerConfig(custom()
                        .minimumNumberOfCalls(10)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofMillis(1500))
                        .slidingWindowSize(2)
                        .build()
                )
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(4))
                        .build()
                ), "frankfurterRatesService");
    }

}
