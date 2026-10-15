package dev.tomaszosuch.currencyconverter.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CurrencyCalculator {

    private static final int PLN_SCALE = 2;

    private CurrencyCalculator() {
    }

    public static BigDecimal toPln(BigDecimal amount, BigDecimal exchangeRate) {
        return amount.multiply(exchangeRate).setScale(PLN_SCALE, RoundingMode.HALF_UP);
    }
}