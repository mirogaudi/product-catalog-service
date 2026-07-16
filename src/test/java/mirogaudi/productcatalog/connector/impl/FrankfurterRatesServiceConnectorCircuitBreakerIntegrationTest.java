package mirogaudi.productcatalog.connector.impl;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import mirogaudi.productcatalog.ProductCatalogServiceApplication;
import mirogaudi.productcatalog.client.FrankfurterRatesService;
import mirogaudi.productcatalog.connector.ConnectorRuntimeException;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static mirogaudi.productcatalog.testhelper.Currencies.EUR;
import static mirogaudi.productcatalog.testhelper.Currencies.USD;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {ProductCatalogServiceApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    // replace the upstream service base URL with the wiremock URL
    "spring.http.serviceclient.frankfurter-rates-service.base-url=http://localhost:7777",
    // disable caching
    "pcs.cache.enabled=false"
})
@EnableWireMock({
    @ConfigureWireMock(name = "mock-frankfurter-rates-service", port = 7777) // set wiremock port
})
class FrankfurterRatesServiceConnectorCircuitBreakerIntegrationTest {

    @InjectWireMock("mock-frankfurter-rates-service")
    static WireMockServer mockFrankfurterRatesService;

    @MockitoSpyBean
    private FrankfurterRatesService ratesService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private FrankfurterRatesServiceConnector ratesServiceConnector;


    @BeforeEach
    void setUp() {
        getCircuitBreaker().reset();
    }

    // repeat -> see slidingWindowSize
    @RepeatedTest(10)
    void getCurrencyExchangeRate_ok_cb_closed() {
        mockFrankfurterRatesService.stubFor(get(urlPathEqualTo("/v2/rates"))
            .withQueryParam("base", equalTo(USD.getCurrencyCode()))
            .withQueryParam("quotes", equalTo(EUR.getCurrencyCode()))
            .willReturn(okJson("""
                [
                  {
                    "date": "2026-07-07",
                    "base": "USD",
                    "quote": "EUR",
                    "rate": 0.87483
                  }
                ]
                """)));

        BigDecimal currencyExchangeRate = ratesServiceConnector.getExchangeRate(USD, EUR);
        assertEquals(0.87483d, currencyExchangeRate.doubleValue());

        assertEquals(CircuitBreaker.State.CLOSED, getCircuitBreaker().getState());

        verify(ratesService).getRates(USD.getCurrencyCode(), EUR.getCurrencyCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "[]"})
    void getCurrencyExchangeRate_not_ok_response_null_or_empty(String json) {
        mockFrankfurterRatesService.stubFor(get(urlPathEqualTo("/v2/rates"))
            .withQueryParam("base", equalTo(USD.getCurrencyCode()))
            .withQueryParam("quotes", equalTo(EUR.getCurrencyCode()))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(json)
            ));

        assertEquals(COUNT_BASED, getCircuitBreaker().getCircuitBreakerConfig().getSlidingWindowType());

        // repeat -> see slidingWindowSize
        for (int repetition = 1; repetition <= 10; repetition++) {
            ConnectorRuntimeException e = assertThrows(ConnectorRuntimeException.class,
                () -> ratesServiceConnector.getExchangeRate(USD, EUR));

            if (repetition <= 5) { // 1st to 5th call -> see minimumNumberOfCalls
                assertInstanceOf(IllegalStateException.class, e.getCause());
                assertEquals("CircuitBreaker: No exchange rate (USD -> EUR) obtained from rates service", e.getMessage());

                if (repetition < 5) {
                    assertEquals(CircuitBreaker.State.CLOSED, getCircuitBreaker().getState());
                } else { // 5th call, circuit breaker opens -> see minimumNumberOfCalls and failureRateThreshold
                    assertEquals(CircuitBreaker.State.OPEN, getCircuitBreaker().getState());

                    // wait till circuit breaker half-opens -> see waitDurationInOpenState, automaticTransitionFromOpenToHalfOpenEnabled
                    await().atLeast(1500, MILLISECONDS).and().atMost(2500, MILLISECONDS)
                        .until(() -> CircuitBreaker.State.HALF_OPEN.equals(getCircuitBreaker().getState()));
                }
            } else if (repetition <= 8) { // 6th to 8th call -> see permittedNumberOfCallsInHalfOpenState
                assertInstanceOf(IllegalStateException.class, e.getCause());
                assertEquals("CircuitBreaker: No exchange rate (USD -> EUR) obtained from rates service", e.getMessage());

                if (repetition < 8) {
                    assertEquals(CircuitBreaker.State.HALF_OPEN, getCircuitBreaker().getState());
                } else { // 8th call, circuit breaker opens -> see permittedNumberOfCallsInHalfOpenState
                    assertEquals(CircuitBreaker.State.OPEN, getCircuitBreaker().getState());
                }
            } else { // 9th to 10th call
                assertInstanceOf(CallNotPermittedException.class, e.getCause());
                assertEquals("CircuitBreaker is OPEN: Refused to obtain exchange rate (USD -> EUR) from rates service", e.getMessage());

                assertEquals(CircuitBreaker.State.OPEN, getCircuitBreaker().getState());
            }
        }

        // ratesService should be called if circuit breaker opened or half-opened -> see minimumNumberOfCalls, permittedNumberOfCallsInHalfOpenState
        verify(ratesService, times(8)).getRates(USD.getCurrencyCode(), EUR.getCurrencyCode());
    }

    @ParameterizedTest
    // 408 (Request Timeout), 429 (Too Many Requests),
    // 500 (Internal Server Error), 502 (Bad Gateway), 503 (Service Unavailable), 504 (Gateway Timeout)
    // TODO check TimeoutException related to 429 and 503 and TimeLimiter
    @ValueSource(ints = {408, 429, 500, 502, 503, 504})
    void getCurrencyExchangeRate_not_ok_server_error(int status) {
        mockFrankfurterRatesService.stubFor(get(urlPathEqualTo("/v2/rates"))
            .withQueryParam("base", equalTo(USD.getCurrencyCode()))
            .withQueryParam("quotes", equalTo(EUR.getCurrencyCode()))
            .willReturn(aResponse().withStatus(status)));

        assertEquals(COUNT_BASED, getCircuitBreaker().getCircuitBreakerConfig().getSlidingWindowType());

        // repeat -> see slidingWindowSize
        for (int repetition = 1; repetition <= 10; repetition++) {
            ConnectorRuntimeException e = assertThrows(ConnectorRuntimeException.class,
                () -> ratesServiceConnector.getExchangeRate(USD, EUR));

            if (repetition <= 5) { // 1st to 5th call -> see minimumNumberOfCalls
                assertInstanceOf(Throwable.class, e.getCause());
                assertTrue(e.getMessage().startsWith(
                    "CircuitBreaker: Failed to obtain exchange rate (USD -> EUR) from rates service. Cause: "));

                if (repetition < 5) {
                    assertEquals(CircuitBreaker.State.CLOSED, getCircuitBreaker().getState());
                } else { // 5th call, circuit breaker opens -> see minimumNumberOfCalls and failureRateThreshold
                    assertEquals(CircuitBreaker.State.OPEN, getCircuitBreaker().getState());

                    // wait till circuit breaker half-opens -> see waitDurationInOpenState, automaticTransitionFromOpenToHalfOpenEnabled
                    await().atLeast(1500, MILLISECONDS).and().atMost(2500, MILLISECONDS)
                        .until(() -> CircuitBreaker.State.HALF_OPEN.equals(getCircuitBreaker().getState()));
                }
            } else if (repetition <= 8) { // 6th to 8th call -> see permittedNumberOfCallsInHalfOpenState
                assertInstanceOf(Throwable.class, e.getCause());
                assertTrue(e.getMessage().startsWith(
                    "CircuitBreaker: Failed to obtain exchange rate (USD -> EUR) from rates service. Cause: "));

                if (repetition < 8) {
                    assertEquals(CircuitBreaker.State.HALF_OPEN, getCircuitBreaker().getState());
                } else { // 8th call, circuit breaker opens -> see permittedNumberOfCallsInHalfOpenState
                    assertEquals(CircuitBreaker.State.OPEN, getCircuitBreaker().getState());
                }
            } else { // 9th to
                // 10th call
                assertInstanceOf(CallNotPermittedException.class, e.getCause());
                assertEquals("CircuitBreaker is OPEN: Refused to obtain exchange rate (USD -> EUR) from rates service", e.getMessage());

                assertEquals(CircuitBreaker.State.OPEN, getCircuitBreaker().getState());
            }
        }

        // ratesService should be called if circuit breaker opened or half-opened -> see minimumNumberOfCalls, permittedNumberOfCallsInHalfOpenState
        verify(ratesService, times(8)).getRates(USD.getCurrencyCode(), EUR.getCurrencyCode());
    }

    private @NonNull CircuitBreaker getCircuitBreaker() {
        return circuitBreakerRegistry.circuitBreaker("cb-frankfurter-rates-service");
    }
}
