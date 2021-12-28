package mirogaudi.demo.productcatalog.connector;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Connector for a currency exchange rates service.
 */
public interface RatesServiceConnector {

    /**
     * Gets currency exchange rate from an external service or from cache since method is cacheable.
     *
     * @param fromCurrency original currency
     * @param toCurrency   target currency
     * @return currency exchange rate
     */
    BigDecimal getCurrencyExchangeRate(Currency fromCurrency,
                                       Currency toCurrency);

}
