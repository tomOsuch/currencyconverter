package dev.tomaszosuch.currencyconverter.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyCalculatorTest {

    @ParameterizedTest(name = "{0} x {1} = {2}")
    @CsvSource({
            "345, 3.7568, 1296.10",
            "543, 3.8025, 2064.76",
            "346, 3.8076, 1317.43",
            "1, 1.0050, 1.01",
            "1, 1.0049, 1.00"
    })
    void shouldConvertAndRoundHalfUpToTwoDecimals(BigDecimal amount, BigDecimal rate, BigDecimal expected) {
        BigDecimal result = CurrencyCalculator.toPln(amount, rate);

        assertThat(result).isEqualTo(expected);
        assertThat(result.scale()).isEqualTo(2);
    }
}