package com.drjcodes.subadvise_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.drjcodes.subadvise_api.dto.CurrencyConversionResponseDto;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.exception.CurrencyRateUnavailableException;
import com.drjcodes.subadvise_api.exception.UnsupportedCurrencyException;
import com.drjcodes.subadvise_api.service.CurrencyRateProvider.RateSnapshot;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionServiceTest {

        @Mock
        private CurrencyRateProvider rateProvider;

        private CurrencyConversionService conversionService;

        @BeforeEach
        void setUp() {
                conversionService = new CurrencyConversionService(rateProvider);
        }

        @Test
        void convertsUsingLatestUsdRatesAndCurrencyFractionDigits() {
                Instant lastUpdated = Instant.parse("2026-10-17T00:00:00Z");
                Instant nextUpdate = Instant.now().plusSeconds(3600);
                when(rateProvider.fetchLatestUsdRates()).thenReturn(new RateSnapshot(
                                Map.of("USD", BigDecimal.ONE, "EUR", new BigDecimal("0.92"), "JPY",
                                                new BigDecimal("150.25")),
                                lastUpdated,
                                nextUpdate));

                CurrencyConversionResponseDto result = conversionService.convert(
                                new BigDecimal("10.00"), Currency.USD, Currency.EUR);

                assertEquals(new BigDecimal("10.00"), result.getAmount());
                assertEquals(Currency.USD, result.getFrom());
                assertEquals(Currency.EUR, result.getTo());
                assertEquals(new BigDecimal("9.20"), result.getConvertedAmount());
                assertEquals(lastUpdated, result.getRateLastUpdated());
                assertEquals(nextUpdate, result.getRateNextUpdate());
                assertEquals("ExchangeRate-API Open Access", result.getRateProvider());
                assertEquals("https://www.exchangerate-api.com", result.getAttributionUrl());
        }

        @Test
        void roundsToTargetCurrencyFractionDigits() {
                when(rateProvider.fetchLatestUsdRates()).thenReturn(new RateSnapshot(
                                Map.of("USD", BigDecimal.ONE, "JPY", new BigDecimal("150.25")),
                                Instant.now(),
                                Instant.now().plusSeconds(3600)));

                CurrencyConversionResponseDto result = conversionService.convert(
                                new BigDecimal("1.00"), Currency.USD, Currency.JPY);

                assertEquals(new BigDecimal("150"), result.getConvertedAmount());
        }

        @Test
        void reusesRatesUntilTheProviderNextUpdateTime() {
                when(rateProvider.fetchLatestUsdRates()).thenReturn(new RateSnapshot(
                                Map.of("USD", BigDecimal.ONE, "EUR", new BigDecimal("0.90")),
                                Instant.now(),
                                Instant.now().plusSeconds(3600)));

                conversionService.convert(BigDecimal.ONE, Currency.USD, Currency.EUR);
                conversionService.convert(BigDecimal.TEN, Currency.USD, Currency.EUR);

                verify(rateProvider).fetchLatestUsdRates();
        }

        @Test
        void rejectsExpiredRatesAndUsesTheFreshSnapshotOnTheNextRequest() {
                RateSnapshot expired = new RateSnapshot(
                                Map.of("USD", BigDecimal.ONE, "EUR", new BigDecimal("0.90")),
                                Instant.EPOCH,
                                Instant.EPOCH);
                RateSnapshot refreshed = new RateSnapshot(
                                Map.of("USD", BigDecimal.ONE, "EUR", new BigDecimal("0.95")),
                                Instant.now(),
                                Instant.now().plusSeconds(3600));
                when(rateProvider.fetchLatestUsdRates()).thenReturn(expired, refreshed);

                assertThrows(CurrencyRateUnavailableException.class,
                                () -> conversionService.convert(BigDecimal.TEN, Currency.USD, Currency.EUR));
                CurrencyConversionResponseDto afterRefresh = conversionService.convert(
                                BigDecimal.TEN, Currency.USD, Currency.EUR);

                assertEquals(new BigDecimal("9.50"), afterRefresh.getConvertedAmount());
                verify(rateProvider, org.mockito.Mockito.times(2)).fetchLatestUsdRates();
        }

        @Test
        void rejectsCurrenciesWithoutProviderRates() {
                when(rateProvider.fetchLatestUsdRates()).thenReturn(new RateSnapshot(
                                Map.of("USD", BigDecimal.ONE),
                                Instant.now(),
                                Instant.now().plusSeconds(3600)));

                assertThrows(UnsupportedCurrencyException.class,
                                () -> conversionService.convert(BigDecimal.ONE, Currency.USD, Currency.BOV));
        }

        @Test
        void rejectsNegativeAmounts() {
                assertThrows(IllegalArgumentException.class,
                                () -> conversionService.convert(new BigDecimal("-1"), Currency.USD, Currency.EUR));
        }
}
