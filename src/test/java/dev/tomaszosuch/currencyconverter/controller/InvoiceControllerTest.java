package dev.tomaszosuch.currencyconverter.controller;

import dev.tomaszosuch.currencyconverter.dto.InvoiceItemResult;
import dev.tomaszosuch.currencyconverter.dto.InvoiceResult;
import dev.tomaszosuch.currencyconverter.dto.UpsertStatus;
import dev.tomaszosuch.currencyconverter.exception.ExchangeRateNotFoundException;
import dev.tomaszosuch.currencyconverter.exception.NbpApiException;
import dev.tomaszosuch.currencyconverter.service.InvoiceService;
import dev.tomaszosuch.currencyconverter.controller.InvoiceController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest {

    private static final String VALID_REQUEST = """
            {
              "items": [
                { "name": "DELL Latitude", "costUsd": 543, "bookingDate": "2026-07-12" }
              ]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvoiceService invoiceService;

    @Test
    void shouldReturnConvertedInvoice() throws Exception {
        given(invoiceService.convertAndSave(anyList())).willReturn(new InvoiceResult(
                List.of(new InvoiceItemResult(
                        "DELL Latitude", LocalDate.of(2026, 7, 12), new BigDecimal("543"),
                        new BigDecimal("3.8025"), LocalDate.of(2026, 7, 10), "132/A/NBP/2026",
                        new BigDecimal("2064.76"), UpsertStatus.CREATED)),
                new BigDecimal("543"),
                new BigDecimal("2064.76")));

        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("DELL Latitude"))
                .andExpect(jsonPath("$.items[0].bookingDate").value("2026-07-12"))
                .andExpect(jsonPath("$.items[0].rateDate").value("2026-07-10"))
                .andExpect(jsonPath("$.items[0].exchangeRate").value(3.8025))
                .andExpect(jsonPath("$.items[0].costPln").value(2064.76))
                .andExpect(jsonPath("$.items[0].status").value("CREATED"))
                .andExpect(jsonPath("$.totalPln").value(2064.76));
    }

    @Test
    void shouldReturnValidationErrorsForInvalidItems() throws Exception {
        String request = """
                {
                  "items": [
                    { "name": " ", "costUsd": -5, "bookingDate": "2999-01-01" }
                  ]
                }
                """;

        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Błąd walidacji"))
                .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder(
                        "items[0].name", "items[0].costUsd", "items[0].bookingDate")));

        verifyNoInteractions(invoiceService);
    }

    @Test
    void shouldRejectEmptyInvoice() throws Exception {
        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "items": [] }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("items"));
    }

    @Test
    void shouldReturnBadRequestForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"items\": [ "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnprocessableWhenNoExchangeRate() throws Exception {
        given(invoiceService.convertAndSave(anyList()))
                .willThrow(new ExchangeRateNotFoundException("USD", LocalDate.of(2026, 7, 12)));

        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Brak kursu waluty"));
    }

    @Test
    void shouldReturnBadGatewayWhenNbpFails() throws Exception {
        given(invoiceService.convertAndSave(anyList()))
                .willThrow(new NbpApiException("NBP down", null));

        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("Błąd serwisu NBP"));
    }
}