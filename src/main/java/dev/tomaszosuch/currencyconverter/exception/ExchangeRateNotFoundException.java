package dev.tomaszosuch.currencyconverter.exception;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ExchangeRateNotFoundException extends RuntimeException {

    private final String currencyCode;
    private final LocalDate bookingDate;

    public ExchangeRateNotFoundException(String currencyCode, LocalDate bookingDate) {
        super("No NBP exchange rate for %s before %s".formatted(currencyCode, bookingDate));
        this.currencyCode = currencyCode;
        this.bookingDate = bookingDate;
    }
}