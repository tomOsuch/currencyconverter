package dev.tomaszosuch.currencyconverter.controller;

import dev.tomaszosuch.currencyconverter.dto.InvoiceRequest;
import dev.tomaszosuch.currencyconverter.dto.InvoiceResult;
import dev.tomaszosuch.currencyconverter.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public InvoiceResult convert(@Valid @RequestBody InvoiceRequest request) {
        return invoiceService.convertAndSave(request.items());
    }
}