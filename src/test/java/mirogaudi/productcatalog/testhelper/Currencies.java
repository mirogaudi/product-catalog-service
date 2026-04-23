package mirogaudi.productcatalog.testhelper;

import java.util.Currency;

public class Currencies {

    public static final Currency USD = getCurrency("USD");
    public static final Currency EUR = getCurrency("EUR");

    public static Currency getCurrency(String currencyCode) {
        return Currency.getInstance(currencyCode);
    }
}
