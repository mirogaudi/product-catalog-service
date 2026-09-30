package mirogaudi.productcatalog.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import mirogaudi.productcatalog.connector.RatesServiceConnector;
import mirogaudi.productcatalog.service.CurrencyExchangeService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Currency;

@Service
@RequiredArgsConstructor
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {

    private final RatesServiceConnector ratesServiceConnector;

    @Override
    public BigDecimal convert(@NonNull BigDecimal amount,
                              @NonNull Currency fromCurrency,
                              @NonNull Currency toCurrency) {

        Assert.isTrue(amount.signum() > 0, "Amount must be positive");

        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        return amount.multiply(ratesServiceConnector.getExchangeRate(fromCurrency, toCurrency));
    }

}
