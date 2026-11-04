package com.drjcodes.subadvise_api.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public interface CurrencyRateProvider {

    RateSnapshot fetchLatestUsdRates();

    record RateSnapshot(Map<String, BigDecimal> rates, Instant lastUpdated, Instant nextUpdate) {
        public RateSnapshot {
            rates = Map.copyOf(rates);
        }
    }
}