package dev.tomaszosuch.currencyconverter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "komputer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Computer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nazwa", nullable = false)
    private String name;

    @Column(name = "data_ksiegowania", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "koszt_usd", nullable = false, precision = 19, scale = 2)
    private BigDecimal costUsd;

    @Column(name = "koszt_pln", nullable = false, precision = 19, scale = 2)
    private BigDecimal costPln;

    @Column(name = "kurs", nullable = false, precision = 10, scale = 4)
    private BigDecimal exchangeRate;

    @Column(name = "data_kursu", nullable = false)
    private LocalDate rateDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Computer(String name, LocalDate bookingDate) {
        this.name = name;
        this.bookingDate = bookingDate;
    }

    public void applyConversion(BigDecimal costUsd, BigDecimal exchangeRate,
                                LocalDate rateDate, BigDecimal costPln) {
        this.costUsd = costUsd;
        this.exchangeRate = exchangeRate;
        this.rateDate = rateDate;
        this.costPln = costPln;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}