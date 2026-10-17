package com.drjcodes.subadvise_api.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.drjcodes.subadvise_api.exception.CurrencyRateUnavailableException;

@Component
public class ExchangeRateApiClient implements CurrencyRateProvider {

    private final RestClient restClient;
    private final String endpoint;

    public ExchangeRateApiClient(
            @Value("${currency.rates.api-url:https://open.er-api.com/v6/latest/USD}") String endpoint) {
        this.restClient = RestClient.create();
        this.endpoint = endpoint;
    }

    @Override
    public RateSnapshot fetchLatestUsdRates() {
        ApiResponse response = restClient.get()
                .uri(endpoint)
                .retrieve()
                .body(ApiResponse.class);

        if (response == null || !"success".equals(response.result())
                || response.rates() == null
                || response.timeLastUpdateUnix() <= 0
                || response.timeNextUpdateUnix() <= 0) {
            throw new CurrencyRateUnavailableException("Exchange rate provider returned an invalid response");
        }

        return new RateSnapshot(
                response.rates(),
                Instant.ofEpochSecond(response.timeLastUpdateUnix()),
                Instant.ofEpochSecond(response.timeNextUpdateUnix()));
    }

    private record ApiResponse(
            String result,
            @JsonProperty("time_last_update_unix") long timeLastUpdateUnix,
            @JsonProperty("time_next_update_unix") long timeNextUpdateUnix,
            Map<String, BigDecimal> rates) {
    }
}