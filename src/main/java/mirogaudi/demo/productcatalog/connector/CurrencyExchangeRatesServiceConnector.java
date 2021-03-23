package mirogaudi.demo.productcatalog.connector;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Connector for a currency exchange rates service.
 */
public interface CurrencyExchangeRatesServiceConnector {

    /**
     * Gets cached currency exchange rate from an external service.
     *
     * @param fromCurrency original currency
     * @param toCurrency   target currency
     * @return currency exchange rate
     */
    BigDecimal getCachedCurrencyExchangeRate(Currency fromCurrency,
                                             Currency toCurrency);

    /**
     * Gets actual currency exchange rate from an external service.
     *
     * @param fromCurrency original currency
     * @param toCurrency   target currency
     * @return currency exchange rate
     */
    BigDecimal getCurrencyExchangeRate(Currency fromCurrency,
                                       Currency toCurrency);

}
