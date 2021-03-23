package mirogaudi.demo.productcatalog.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import mirogaudi.demo.productcatalog.connector.CurrencyExchangeRatesServiceConnector;
import mirogaudi.demo.productcatalog.service.CurrencyExchangeService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Currency;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {

    private final CurrencyExchangeRatesServiceConnector ratesServiceConnector;

    @Override
    public Mono<BigDecimal> convert(@NonNull BigDecimal amount,
                                    @NonNull Currency fromCurrency,
                                    @NonNull Currency toCurrency) {
        if (fromCurrency == toCurrency) {
            return Mono.just(amount);
        }

        return Mono.fromCallable(() -> ratesServiceConnector.getCachedCurrencyExchangeRate(fromCurrency, toCurrency))
                .map(amount::multiply);
    }

}
