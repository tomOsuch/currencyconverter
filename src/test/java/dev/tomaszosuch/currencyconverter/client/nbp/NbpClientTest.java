package dev.tomaszosuch.currencyconverter.client.nbp;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import dev.tomaszosuch.currencyconverter.config.NbpClientConfig;
import dev.tomaszosuch.currencyconverter.config.NbpProperties;
import dev.tomaszosuch.currencyconverter.exception.NbpApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NbpClientTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 7, 2);
    private static final LocalDate END_DATE = LocalDate.of(2026, 7, 11);
    private static final String RATES_URL = "/api/exchangerates/rates/A/USD/2026-07-02/2026-07-11/";

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private NbpClient nbpClient;

    @BeforeEach
    void setUp() {
        NbpProperties properties = new NbpProperties(
                wireMock.baseUrl(), Duration.ofSeconds(1), Duration.ofMillis(500), 10);
        nbpClient = new NbpClient(new NbpClientConfig().nbpRestClient(properties));
    }

    @Test
    void shouldReturnRatesFromNbpResponse() {
        wireMock.stubFor(get(urlEqualTo(RATES_URL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "table": "A",
                                  "currency": "dolar amerykański",
                                  "code": "USD",
                                  "rates": [
                                    {"no": "126/A/NBP/2026", "effectiveDate": "2026-07-02", "mid": 3.7568},
                                    {"no": "132/A/NBP/2026", "effectiveDate": "2026-07-10", "mid": 3.8025}
                                  ]
                                }
                                """)));

        List<NbpRatesResponse.Rate> rates = nbpClient.getMidRates("USD", START_DATE, END_DATE);

        assertThat(rates).hasSize(2);
        assertThat(rates.get(1).no()).isEqualTo("132/A/NBP/2026");
        assertThat(rates.get(1).effectiveDate()).isEqualTo(LocalDate.of(2026, 7, 10));
        assertThat(rates.get(1).mid()).isEqualByComparingTo(new BigDecimal("3.8025"));

        wireMock.verify(getRequestedFor(urlEqualTo(RATES_URL))
                .withHeader("Accept", equalTo("application/json")));
    }

    @Test
    void shouldReturnEmptyListWhenNbpHasNoData() {
        wireMock.stubFor(get(urlEqualTo(RATES_URL))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("404 NotFound - Not Found - Brak danych")));

        List<NbpRatesResponse.Rate> rates = nbpClient.getMidRates("USD", START_DATE, END_DATE);

        assertThat(rates).isEmpty();
    }

    @Test
    void shouldThrowNbpApiExceptionOnServerError() {
        wireMock.stubFor(get(urlEqualTo(RATES_URL))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> nbpClient.getMidRates("USD", START_DATE, END_DATE))
                .isInstanceOf(NbpApiException.class)
                .hasMessageContaining("USD");
    }

    @Test
    void shouldThrowNbpApiExceptionOnBadRequest() {
        wireMock.stubFor(get(urlEqualTo(RATES_URL))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("400 BadRequest - Przekroczony limit 93 dni")));

        assertThatThrownBy(() -> nbpClient.getMidRates("USD", START_DATE, END_DATE))
                .isInstanceOf(NbpApiException.class);
    }

    @Test
    void shouldThrowNbpApiExceptionOnReadTimeout() {
        wireMock.stubFor(get(urlEqualTo(RATES_URL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(1_000)));

        assertThatThrownBy(() -> nbpClient.getMidRates("USD", START_DATE, END_DATE))
                .isInstanceOf(NbpApiException.class);
    }
}