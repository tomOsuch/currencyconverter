package dev.tomaszosuch.currencyconverter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRate(
        String currencyCode,
        BigDecimal rate,
        LocalDate effectiveDate,
        String tableNo
) {
}
