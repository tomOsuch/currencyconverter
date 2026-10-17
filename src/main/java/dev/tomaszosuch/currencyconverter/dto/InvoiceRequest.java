package dev.tomaszosuch.currencyconverter.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InvoiceRequest(

        @NotEmpty(message = "Faktura musi zawierać co najmniej jedną pozycję")
        @Size(max = 50, message = "Faktura może zawierać maksymalnie 50 pozycji")
        List<@Valid InvoiceItemRequest> items
) {
}