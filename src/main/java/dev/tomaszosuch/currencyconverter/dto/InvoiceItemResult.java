package dev.tomaszosuch.currencyconverter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceItemResult(
        String name,
        LocalDate bookingDate,
        BigDecimal costUsd,
        BigDecimal exchangeRate,
        LocalDate rateDate,
        String tableNo,
        BigDecimal costPln,
        UpsertStatus status
) {

    public static InvoiceItemResult of(ConvertedItem item, UpsertStatus status) {
        return new InvoiceItemResult(
                item.name(),
                item.bookingDate(),
                item.costUsd(),
                item.exchangeRate().rate(),
                item.exchangeRate().effectiveDate(),
                item.exchangeRate().tableNo(),
                item.costPln(),
                status
        );
    }
}