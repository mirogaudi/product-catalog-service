package mirogaudi.productcatalog.client;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.time.LocalDate;

// For API details See https://frankfurter.dev/
@HttpExchange(url = "/v2/rates")
public interface FrankfurterRatesService {

    record Rate(@JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                String base,
                String quote,
                double rate) {
    }

    @GetExchange
    Rate[] getRates(@RequestParam String base,
                    @RequestParam String quotes);

}
