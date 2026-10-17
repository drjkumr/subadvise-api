package com.drjcodes.subadvise_api.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.drjcodes.subadvise_api.dto.CurrencyConversionResponseDto;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.exception.CurrencyRateUnavailableException;
import com.drjcodes.subadvise_api.exception.UnsupportedCurrencyException;
import com.drjcodes.subadvise_api.service.CurrencyRateProvider.RateSnapshot;

@Service
public class CurrencyConversionService {

    private static final String RATE_PROVIDER = "ExchangeRate-API Open Access";
    private static final String ATTRIBUTION_URL = "https://www.exchangerate-api.com";

    private final CurrencyRateProvider rateProvider;
    private volatile RateSnapshot cachedRates;

    public CurrencyConversionService(CurrencyRateProvider rateProvider) {
        this.rateProvider = rateProvider;
    }

    public CurrencyConversionResponseDto convert(BigDecimal amount, Currency from, Currency to) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Amount must be zero or greater");
        }

        RateSnapshot rates = currentRates();
        BigDecimal fromRate = findRate(rates, from);
        BigDecimal toRate = findRate(rates, to);
        BigDecimal conversionRate = toRate.divide(fromRate, MathContext.DECIMAL128);
        BigDecimal convertedAmount = amount.multiply(conversionRate)
                .setScale(fractionDigits(to), RoundingMode.HALF_UP);

        CurrencyConversionResponseDto response = new CurrencyConversionResponseDto();
        response.setAmount(amount);
        response.setFrom(from);
        response.setTo(to);
        response.setConvertedAmount(convertedAmount);
        response.setRateLastUpdated(rates.lastUpdated());
        response.setRateNextUpdate(rates.nextUpdate());
        response.setRateProvider(RATE_PROVIDER);
        response.setAttributionUrl(ATTRIBUTION_URL);
        return response;
    }

    private RateSnapshot currentRates() {
        RateSnapshot snapshot = cachedRates;
        if (snapshot != null && Instant.now().isBefore(snapshot.nextUpdate())) {
            return snapshot;
        }

        synchronized (this) {
            snapshot = cachedRates;
            if (snapshot != null && Instant.now().isBefore(snapshot.nextUpdate())) {
                return snapshot;
            }

            try {
                snapshot = rateProvider.fetchLatestUsdRates();
            } catch (RuntimeException exception) {
                throw new CurrencyRateUnavailableException("Could not retrieve current exchange rates", exception);
            }

            if (snapshot == null || snapshot.nextUpdate() == null || snapshot.lastUpdated() == null) {
                throw new CurrencyRateUnavailableException("Exchange rate provider returned invalid timestamps");
            }
            if (!snapshot.nextUpdate().isAfter(Instant.now())) {
                throw new CurrencyRateUnavailableException("Exchange rate provider returned expired rates");
            }

            cachedRates = snapshot;
            return snapshot;
        }
    }

    private BigDecimal findRate(RateSnapshot rates, Currency currency) {
        BigDecimal rate = rates.rates().get(currency.name());
        if (rate == null || rate.signum() <= 0) {
            throw new UnsupportedCurrencyException(
                    "No current exchange rate is available for currency " + currency.name());
        }
        return rate;
    }

    private int fractionDigits(Currency currency) {
        try {
            int digits = java.util.Currency.getInstance(currency.name()).getDefaultFractionDigits();
            return digits >= 0 ? digits : 2;
        } catch (IllegalArgumentException exception) {
            return 2;
        }
    }
}