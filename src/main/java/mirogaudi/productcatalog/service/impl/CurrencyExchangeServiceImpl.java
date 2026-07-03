package mirogaudi.productcatalog.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import mirogaudi.productcatalog.connector.RatesServiceConnector;
import mirogaudi.productcatalog.service.CurrencyExchangeService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Currency;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {

    private final RatesServiceConnector ratesServiceConnector;

    @Override
    public BigDecimal convert(@NonNull BigDecimal amount,
                              @NonNull Currency fromCurrency,
                              @NonNull Currency toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        return amount.multiply(ratesServiceConnector.getCurrencyExchangeRate(fromCurrency, toCurrency));
    }

}
