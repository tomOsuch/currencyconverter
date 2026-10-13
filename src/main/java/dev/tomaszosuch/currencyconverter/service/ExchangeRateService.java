package dev.tomaszosuch.currencyconverter.service;

import dev.tomaszosuch.currencyconverter.client.nbp.NbpClient;
import dev.tomaszosuch.currencyconverter.client.nbp.NbpRatesResponse;
import dev.tomaszosuch.currencyconverter.config.NbpProperties;
import dev.tomaszosuch.currencyconverter.dto.ExchangeRate;
import dev.tomaszosuch.currencyconverter.exception.ExchangeRateNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final NbpClient nbpClient;
    private final NbpProperties nbpProperties;

    public ExchangeRate getRateForBookingDate(String currencyCode, LocalDate bookingDate) {
        LocalDate endDate = bookingDate.minusDays(1);
        LocalDate startDate = bookingDate.minusDays(nbpProperties.lookbackDays());

        return nbpClient.getMidRates(currencyCode, startDate, endDate).stream()
                .max(Comparator.comparing(NbpRatesResponse.Rate::effectiveDate))
                .map(rate -> new ExchangeRate(currencyCode, rate.mid(), rate.effectiveDate(), rate.no()))
                .orElseThrow(() -> new ExchangeRateNotFoundException(currencyCode, bookingDate));
    }
}