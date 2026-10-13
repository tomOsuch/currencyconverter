package dev.tomaszosuch.currencyconverter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "nbp")
public record NbpProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        int lookbackDays
) {
}