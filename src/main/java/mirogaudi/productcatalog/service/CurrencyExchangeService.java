package mirogaudi.productcatalog.service;


import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Service for currency conversions.
 */
public interface CurrencyExchangeService {

    /**
     * Converts a given amount from one currency to another.
     *
     * @param amount       an amount to convert
     * @param fromCurrency original currency
     * @param toCurrency   target currency
     * @return converted amount
     */
    Mono<BigDecimal> convert(BigDecimal amount,
                             Currency fromCurrency,
                             Currency toCurrency);

}
