package dev.tomaszosuch.currencyconverter.service;

import dev.tomaszosuch.currencyconverter.dto.ConvertedItem;
import dev.tomaszosuch.currencyconverter.dto.ExchangeRate;
import dev.tomaszosuch.currencyconverter.dto.InvoiceItemRequest;
import dev.tomaszosuch.currencyconverter.dto.InvoiceItemResult;
import dev.tomaszosuch.currencyconverter.dto.InvoiceResult;
import dev.tomaszosuch.currencyconverter.dto.UpsertStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final String USD = "USD";

    private final ExchangeRateService exchangeRateService;
    private final ComputerService computerService;

    public InvoiceResult convertAndSave(List<InvoiceItemRequest> requests) {
        List<ConvertedItem> converted = requests.stream()
                .map(this::convert)
                .toList();

        List<UpsertStatus> statuses = computerService.upsertAll(converted);

        List<InvoiceItemResult> items = IntStream.range(0, converted.size())
                .mapToObj(i -> InvoiceItemResult.of(converted.get(i), statuses.get(i)))
                .toList();

        return new InvoiceResult(
                items,
                sum(items, InvoiceItemResult::costUsd),
                sum(items, InvoiceItemResult::costPln));
    }

    private ConvertedItem convert(InvoiceItemRequest request) {
        ExchangeRate rate = exchangeRateService.getRateForBookingDate(USD, request.bookingDate());
        BigDecimal costPln = CurrencyCalculator.toPln(request.costUsd(), rate.rate());
        return new ConvertedItem(request.name().trim(), request.bookingDate(), request.costUsd(), rate, costPln);
    }

    private static BigDecimal sum(List<InvoiceItemResult> items, Function<InvoiceItemResult, BigDecimal> field) {
        return items.stream()
                .map(field)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}