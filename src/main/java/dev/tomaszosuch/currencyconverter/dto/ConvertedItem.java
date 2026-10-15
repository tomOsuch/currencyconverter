package dev.tomaszosuch.currencyconverter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConvertedItem(
        String name,
        LocalDate bookingDate,
        BigDecimal costUsd,
        ExchangeRate exchangeRate,
        BigDecimal costPln
) {
}
