package dev.tomaszosuch.currencyconverter.client.nbp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NbpRatesResponse(
        String table,
        String currency,
        String code,
        List<Rate> rates
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Rate(
            String no,
            LocalDate effectiveDate,
            BigDecimal mid
    ) {
    }
}
