package dev.tomaszosuch.currencyconverter.repository;

import dev.tomaszosuch.currencyconverter.entity.Computer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ComputerRepository extends JpaRepository<Computer, Long> {

    Optional<Computer> findByNameAndBookingDate(String name, LocalDate bookingDate);
}
