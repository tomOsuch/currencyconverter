package dev.tomaszosuch.currencyconverter.service;

import dev.tomaszosuch.currencyconverter.dto.ConvertedItem;
import dev.tomaszosuch.currencyconverter.dto.ExchangeRate;
import dev.tomaszosuch.currencyconverter.dto.UpsertStatus;
import dev.tomaszosuch.currencyconverter.entity.Computer;
import dev.tomaszosuch.currencyconverter.repository.ComputerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ComputerServiceTest {

    private static final LocalDate BOOKING_DATE = LocalDate.of(2026, 7, 12);
    private static final ConvertedItem DELL = new ConvertedItem(
            "DELL Latitude",
            BOOKING_DATE,
            new BigDecimal("543"),
            new ExchangeRate("USD", new BigDecimal("3.8025"), LocalDate.of(2026, 7, 10), "132/A/NBP/2026"),
            new BigDecimal("2064.76"));

    @Mock
    private ComputerRepository computerRepository;

    @InjectMocks
    private ComputerService computerService;

    @Test
    void shouldCreateComputerWhenNotExists() {
        given(computerRepository.findByNameAndBookingDate("DELL Latitude", BOOKING_DATE))
                .willReturn(Optional.empty());

        List<UpsertStatus> statuses = computerService.upsertAll(List.of(DELL));

        assertThat(statuses).containsExactly(UpsertStatus.CREATED);

        ArgumentCaptor<Computer> captor = ArgumentCaptor.forClass(Computer.class);
        verify(computerRepository).save(captor.capture());
        Computer saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("DELL Latitude");
        assertThat(saved.getBookingDate()).isEqualTo(BOOKING_DATE);
        assertThat(saved.getCostUsd()).isEqualByComparingTo("543");
        assertThat(saved.getExchangeRate()).isEqualByComparingTo("3.8025");
        assertThat(saved.getRateDate()).isEqualTo(LocalDate.of(2026, 7, 10));
        assertThat(saved.getCostPln()).isEqualByComparingTo("2064.76");
    }

    @Test
    void shouldUpdateExistingComputer() {
        Computer existing = new Computer("DELL Latitude", BOOKING_DATE);
        existing.applyConversion(new BigDecimal("500"), new BigDecimal("3.7000"),
                LocalDate.of(2026, 7, 9), new BigDecimal("1850.00"));
        given(computerRepository.findByNameAndBookingDate("DELL Latitude", BOOKING_DATE))
                .willReturn(Optional.of(existing));

        List<UpsertStatus> statuses = computerService.upsertAll(List.of(DELL));

        assertThat(statuses).containsExactly(UpsertStatus.UPDATED);
        verify(computerRepository).save(existing);
        assertThat(existing.getCostUsd()).isEqualByComparingTo("543");
        assertThat(existing.getExchangeRate()).isEqualByComparingTo("3.8025");
        assertThat(existing.getRateDate()).isEqualTo(LocalDate.of(2026, 7, 10));
        assertThat(existing.getCostPln()).isEqualByComparingTo("2064.76");
    }
}