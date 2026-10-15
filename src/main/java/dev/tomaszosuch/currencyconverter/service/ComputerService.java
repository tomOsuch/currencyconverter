package dev.tomaszosuch.currencyconverter.service;

import dev.tomaszosuch.currencyconverter.dto.ConvertedItem;
import dev.tomaszosuch.currencyconverter.dto.UpsertStatus;
import dev.tomaszosuch.currencyconverter.entity.Computer;
import dev.tomaszosuch.currencyconverter.repository.ComputerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComputerService {

    private final ComputerRepository computerRepository;

    @Transactional
    public List<UpsertStatus> upsertAll(List<ConvertedItem> items) {
        return items.stream()
                .map(this::upsert)
                .toList();
    }

    private UpsertStatus upsert(ConvertedItem item) {
        Optional<Computer> existing =
                computerRepository.findByNameAndBookingDate(item.name(), item.bookingDate());

        Computer computer = existing.orElseGet(() -> new Computer(item.name(), item.bookingDate()));
        computer.applyConversion(
                item.costUsd(),
                item.exchangeRate().rate(),
                item.exchangeRate().effectiveDate(),
                item.costPln());
        computerRepository.save(computer);

        return existing.isPresent() ? UpsertStatus.UPDATED : UpsertStatus.CREATED;
    }
}