package dev.tomaszosuch.currencyconverter.service;

import dev.tomaszosuch.currencyconverter.dto.ConvertedItem;
import dev.tomaszosuch.currencyconverter.dto.ExchangeRate;
import dev.tomaszosuch.currencyconverter.dto.InvoiceItemRequest;
import dev.tomaszosuch.currencyconverter.dto.InvoiceResult;
import dev.tomaszosuch.currencyconverter.dto.UpsertStatus;
import dev.tomaszosuch.currencyconverter.exception.NbpApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    private static final LocalDate ACER_DATE = LocalDate.of(2026, 7, 3);
    private static final LocalDate DELL_DATE = LocalDate.of(2026, 7, 12);
    private static final LocalDate HP_DATE = LocalDate.of(2026, 7, 15);

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private ComputerService computerService;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void shouldConvertInvoiceFromTaskAndSumRoundedItems() {
        given(exchangeRateService.getRateForBookingDate("USD", ACER_DATE))
                .willReturn(rate("3.7568", "2026-07-02", "126/A/NBP/2026"));
        given(exchangeRateService.getRateForBookingDate("USD", DELL_DATE))
                .willReturn(rate("3.8025", "2026-07-10", "132/A/NBP/2026"));
        given(exchangeRateService.getRateForBookingDate("USD", HP_DATE))
                .willReturn(rate("3.8076", "2026-07-14", "134/A/NBP/2026"));
        given(computerService.upsertAll(anyList()))
                .willReturn(List.of(UpsertStatus.CREATED, UpsertStatus.UPDATED, UpsertStatus.CREATED));

        InvoiceResult result = invoiceService.convertAndSave(List.of(
                new InvoiceItemRequest("ACER Aspire", new BigDecimal("345"), ACER_DATE),
                new InvoiceItemRequest("DELL Latitude", new BigDecimal("543"), DELL_DATE),
                new InvoiceItemRequest("HP Victus", new BigDecimal("346"), HP_DATE)));

        assertThat(result.items()).hasSize(3);
        assertThat(result.items().get(0).costPln()).isEqualByComparingTo("1296.10");
        assertThat(result.items().get(1).costPln()).isEqualByComparingTo("2064.76");
        assertThat(result.items().get(2).costPln()).isEqualByComparingTo("1317.43");
        assertThat(result.items().get(1).rateDate()).isEqualTo(LocalDate.of(2026, 7, 10));
        assertThat(result.items().get(1).status()).isEqualTo(UpsertStatus.UPDATED);
        assertThat(result.totalUsd()).isEqualByComparingTo("1234");
        assertThat(result.totalPln()).isEqualByComparingTo("4678.29");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldTrimNameBeforeSaving() {
        given(exchangeRateService.getRateForBookingDate("USD", ACER_DATE))
                .willReturn(rate("3.7568", "2026-07-02", "126/A/NBP/2026"));
        given(computerService.upsertAll(anyList())).willReturn(List.of(UpsertStatus.CREATED));

        invoiceService.convertAndSave(List.of(
                new InvoiceItemRequest("  ACER Aspire ", new BigDecimal("345"), ACER_DATE)));

        ArgumentCaptor<List<ConvertedItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(computerService).upsertAll(captor.capture());
        assertThat(captor.getValue().getFirst().name()).isEqualTo("ACER Aspire");
    }

    @Test
    void shouldNotSaveAnythingWhenNbpFails() {
        given(exchangeRateService.getRateForBookingDate("USD", ACER_DATE))
                .willReturn(rate("3.7568", "2026-07-02", "126/A/NBP/2026"));
        given(exchangeRateService.getRateForBookingDate("USD", DELL_DATE))
                .willThrow(new NbpApiException("NBP down", null));

        assertThatThrownBy(() -> invoiceService.convertAndSave(List.of(
                new InvoiceItemRequest("ACER Aspire", new BigDecimal("345"), ACER_DATE),
                new InvoiceItemRequest("DELL Latitude", new BigDecimal("543"), DELL_DATE))))
                .isInstanceOf(NbpApiException.class);

        verify(computerService, never()).upsertAll(any());
    }

    private static ExchangeRate rate(String mid, String effectiveDate, String tableNo) {
        return new ExchangeRate("USD", new BigDecimal(mid), LocalDate.parse(effectiveDate), tableNo);
    }
}