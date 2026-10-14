package dev.tomaszosuch.currencyconverter.client.nbp;

import dev.tomaszosuch.currencyconverter.exception.NbpApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NbpClient {

    private static final String TABLE_A = "A";
    private static final String RATES_PATH = "/api/exchangerates/rates/{table}/{code}/{startDate}/{endDate}/";

    private final RestClient nbpRestClient;

    public List<NbpRatesResponse.Rate> getMidRates(String currencyCode, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching NBP rates for {} from {} to {}", currencyCode, startDate, endDate);
        try {
            NbpRatesResponse response = nbpRestClient.get()
                    .uri(RATES_PATH, TABLE_A, currencyCode, startDate, endDate)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(NbpRatesResponse.class);

            return response == null || response.rates() == null ? List.of() : response.rates();
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("No NBP rates for {} from {} to {}", currencyCode, startDate, endDate);
            return List.of();
        } catch (RestClientException e) {
            throw new NbpApiException(
                    "NBP API request failed for %s from %s to %s".formatted(currencyCode, startDate, endDate), e);
        }
    }
}