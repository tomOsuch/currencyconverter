package dev.tomaszosuch.currencyconverter.exception;

import java.time.LocalDate;

public class ExchangeRateNotFoundException extends RuntimeException {

    public ExchangeRateNotFoundException(String currencyCode, LocalDate bookingDate) {
        super("No NBP exchange rate for %s before %s".formatted(currencyCode, bookingDate));
    }
}
