package dev.tomaszosuch.currencyconverter.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceItemRequest(

        @NotBlank(message = "Nazwa komputera jest wymagana")
        @Size(max = 255, message = "Nazwa może mieć maksymalnie 255 znaków")
        String name,

        @NotNull(message = "Kwota USD jest wymagana")
        @Positive(message = "Kwota USD musi być większa od zera")
        @Digits(integer = 17, fraction = 2, message = "Kwota USD może mieć maksymalnie 2 miejsca po przecinku")
        BigDecimal costUsd,

        @NotNull(message = "Data księgowania jest wymagana")
        @PastOrPresent(message = "Data księgowania nie może być z przyszłości")
        LocalDate bookingDate
) {
}