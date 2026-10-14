package dev.tomaszosuch.currencyconverter.service;

import dev.tomaszosuch.currencyconverter.client.nbp.NbpClient;
import dev.tomaszosuch.currencyconverter.client.nbp.NbpRatesResponse.Rate;
import dev.tomaszosuch.currencyconverter.config.NbpProperties;
import dev.tomaszosuch.currencyconverter.dto.ExchangeRate;
import dev.tomaszosuch.currencyconverter.exception.ExchangeRateNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    private static final String USD = "USD";
    private static final int LOOKBACK_DAYS = 10;

    /**
     * Real NBP table A USD rates (22.06–14.07.2026). Weekends have no rates, as in the real API.
     */
    private static final List<Rate> NBP_USD_RATES = List.of(
            rate("118/A/NBP/2026", "2026-06-22", "3.7282"),
            rate("119/A/NBP/2026", "2026-06-23", "3.7554"),
            rate("120/A/NBP/2026", "2026-06-24", "3.7756"),
            rate("121/A/NBP/2026", "2026-06-25", "3.7720"),
            rate("122/A/NBP/2026", "2026-06-26", "3.7599"),
            rate("123/A/NBP/2026", "2026-06-29", "3.7636"),
            rate("124/A/NBP/2026", "2026-06-30", "3.7708"),
            rate("125/A/NBP/2026", "2026-07-01", "3.7708"),
            rate("126/A/NBP/2026", "2026-07-02", "3.7568"),
            rate("127/A/NBP/2026", "2026-07-03", "3.7420"),
            rate("128/A/NBP/2026", "2026-07-06", "3.7540"),
            rate("129/A/NBP/2026", "2026-07-07", "3.7573"),
            rate("130/A/NBP/2026", "2026-07-08", "3.7755"),
            rate("131/A/NBP/2026", "2026-07-09", "3.7692"),
            rate("132/A/NBP/2026", "2026-07-10", "3.8025"),
            rate("133/A/NBP/2026", "2026-07-13", "3.7846"),
            rate("134/A/NBP/2026", "2026-07-14", "3.8076")
    );

    @Mock
    private NbpClient nbpClient;

    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        NbpProperties properties = new NbpProperties(
                "http://unused", Duration.ofSeconds(1), Duration.ofSeconds(1), LOOKBACK_DAYS);
        exchangeRateService = new ExchangeRateService(nbpClient, properties);
    }

    @ParameterizedTest(name = "booking {0} -> rate from {1}")
    @CsvSource({
            "2026-07-03, 2026-07-02, 3.7568, 126/A/NBP/2026",  // ACER: Friday, rate from Thursday (not from booking day)
            "2026-07-12, 2026-07-10, 3.8025, 132/A/NBP/2026",  // DELL: Sunday, rate from Friday
            "2026-07-15, 2026-07-14, 3.8076, 134/A/NBP/2026",  // HP: Wednesday, rate from Tuesday
            "2026-07-06, 2026-07-03, 3.7420, 127/A/NBP/2026"   // Monday, rate from previous Friday
    })
    void shouldReturnRateFromLastBusinessDayBeforeBookingDate(
            LocalDate bookingDate, LocalDate expectedRateDate, BigDecimal expectedRate, String expectedTableNo) {
        stubNbpWithRealRates();

        ExchangeRate result = exchangeRateService.getRateForBookingDate(USD, bookingDate);

        assertThat(result.currencyCode()).isEqualTo(USD);
        assertThat(result.effectiveDate()).isEqualTo(expectedRateDate);
        assertThat(result.rate()).isEqualByComparingTo(expectedRate);
        assertThat(result.tableNo()).isEqualTo(expectedTableNo);
        verify(nbpClient).getMidRates(USD, bookingDate.minusDays(LOOKBACK_DAYS), bookingDate.minusDays(1));
    }

    @Test
    void shouldPickLatestRateRegardlessOfResponseOrder() {
        LocalDate bookingDate = LocalDate.of(2026, 7, 12);
        given(nbpClient.getMidRates(eq(USD), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(List.of(
                        rate("132/A/NBP/2026", "2026-07-10", "3.8025"),
                        rate("126/A/NBP/2026", "2026-07-02", "3.7568"),
                        rate("130/A/NBP/2026", "2026-07-08", "3.7755")));

        ExchangeRate result = exchangeRateService.getRateForBookingDate(USD, bookingDate);

        assertThat(result.effectiveDate()).isEqualTo(LocalDate.of(2026, 7, 10));
    }

    @Test
    void shouldThrowWhenNoRatesInLookbackRange() {
        LocalDate bookingDate = LocalDate.of(2002, 1, 2);
        given(nbpClient.getMidRates(eq(USD), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(List.of());

        assertThatThrownBy(() -> exchangeRateService.getRateForBookingDate(USD, bookingDate))
                .isInstanceOf(ExchangeRateNotFoundException.class)
                .hasMessageContaining("USD")
                .hasMessageContaining("2002-01-02");
    }

    private void stubNbpWithRealRates() {
        given(nbpClient.getMidRates(eq(USD), any(LocalDate.class), any(LocalDate.class)))
                .willAnswer(invocation -> {
                    LocalDate startDate = invocation.getArgument(1);
                    LocalDate endDate = invocation.getArgument(2);
                    return NBP_USD_RATES.stream()
                            .filter(rate -> !rate.effectiveDate().isBefore(startDate)
                                    && !rate.effectiveDate().isAfter(endDate))
                            .toList();
                });
    }

    private static Rate rate(String no, String effectiveDate, String mid) {
        return new Rate(no, LocalDate.parse(effectiveDate), new BigDecimal(mid));
    }
}