package dev.tomaszosuch.currencyconverter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceItemRequest(
        String name,
        BigDecimal costUsd,
        LocalDate bookingDate
) {
}
