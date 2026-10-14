package dev.tomaszosuch.currencyconverter.dto;

import java.math.BigDecimal;
import java.util.List;

public record InvoiceResult(
        List<InvoiceItemResult> items,
        BigDecimal totalUsd,
        BigDecimal totalPln
) {
}