package com.drjcodes.subadvise_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.drjcodes.subadvise_api.dto.CurrencyConversionResponseDto;
import com.drjcodes.subadvise_api.dto.SubscriptionCurrencyConversionResponseDto;
import com.drjcodes.subadvise_api.dto.SubscriptionResponseDto;
import com.drjcodes.subadvise_api.exception.SubscriptionNotFoundException;
import com.drjcodes.subadvise_api.exception.UnsupportedCurrencyException;
import com.drjcodes.subadvise_api.mapper.SubscriptionMapper;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.model.Subscription;
import com.drjcodes.subadvise_api.repository.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
class SubscriptionCurrencyServiceTest {

        @Mock
        private SubscriptionRepository subscriptionRepository;

        @Mock
        private SubscriptionMapper subscriptionMapper;

        @Mock
        private CurrencyConversionService currencyConversionService;

        private SubscriptionCurrencyService subscriptionCurrencyService;

        @BeforeEach
        void setUp() {
                subscriptionCurrencyService = new SubscriptionCurrencyService(
                                subscriptionRepository, subscriptionMapper, currencyConversionService);
        }

        @Test
        void convertsAndPersistsSubscriptionPriceAndCurrency() {
                UUID id = UUID.randomUUID();
                Subscription subscription = subscription(id);
                CurrencyConversionResponseDto conversion = conversionResponse();
                SubscriptionResponseDto updatedDto = new SubscriptionResponseDto();
                updatedDto.setSubscriptionId(id);
                updatedDto.setSubscriptionPrice(new BigDecimal("9.20"));
                updatedDto.setCurrency(Currency.EUR);
                when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscription));
                when(currencyConversionService.convert(new BigDecimal("10.00"), Currency.USD, Currency.EUR))
                                .thenReturn(conversion);
                when(subscriptionRepository.save(org.mockito.ArgumentMatchers.any(Subscription.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
                when(subscriptionMapper.toResponseDto(org.mockito.ArgumentMatchers.any(Subscription.class)))
                                .thenReturn(updatedDto);

                SubscriptionCurrencyConversionResponseDto result = subscriptionCurrencyService
                                .convertSubscriptionCurrency(id,
                                                Currency.EUR);

                assertEquals(new BigDecimal("9.20"), subscription.getSubscriptionPrice());
                assertEquals(Currency.EUR, subscription.getCurrency());
                assertSame(updatedDto, result.getSubscription());
                assertSame(conversion, result.getConversion());
                verify(subscriptionRepository, never()).delete(subscription);
                verify(subscriptionRepository).save(subscription);
                verify(subscriptionRepository).save(org.mockito.ArgumentMatchers.argThat(saved ->
                                saved.getSubscriptionId().equals(id)
                                                && saved.getSubscriptionPrice().compareTo(new BigDecimal("9.20")) == 0
                                                && saved.getCurrency() == Currency.EUR));
        }

        @Test
        void doesNotSaveWhenRateConversionFails() {
                UUID id = UUID.randomUUID();
                Subscription subscription = subscription(id);
                when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscription));
                when(currencyConversionService.convert(new BigDecimal("10.00"), Currency.USD, Currency.BOV))
                                .thenThrow(new UnsupportedCurrencyException("No rate"));

                assertThrows(UnsupportedCurrencyException.class,
                                () -> subscriptionCurrencyService.convertSubscriptionCurrency(id, Currency.BOV));

                assertEquals(new BigDecimal("10.00"), subscription.getSubscriptionPrice());
                assertEquals(Currency.USD, subscription.getCurrency());
                verify(subscriptionRepository, never()).save(subscription);
        }

        @Test
        void throwsWhenSubscriptionDoesNotExist() {
                UUID id = UUID.randomUUID();
                when(subscriptionRepository.findById(id)).thenReturn(Optional.empty());

                assertThrows(SubscriptionNotFoundException.class,
                                () -> subscriptionCurrencyService.convertSubscriptionCurrency(id, Currency.EUR));

                verify(currencyConversionService, never()).convert(
                                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                                org.mockito.ArgumentMatchers.any());
                verify(subscriptionRepository, never()).save(org.mockito.ArgumentMatchers.any());
        }

        private Subscription subscription(UUID id) {
                return Subscription.builder()
                                .subscriptionId(id)
                                .subscriptionName("Streaming")
                                .subscriptionDate(LocalDate.of(2025, 1, 1))
                                .renewalDate(LocalDate.of(2025, 2, 1))
                                .subscriptionPrice(new BigDecimal("10.00"))
                                .currency(Currency.USD)
                                .build();
        }

        private CurrencyConversionResponseDto conversionResponse() {
                CurrencyConversionResponseDto response = new CurrencyConversionResponseDto();
                response.setAmount(new BigDecimal("10.00"));
                response.setFrom(Currency.USD);
                response.setTo(Currency.EUR);
                response.setConvertedAmount(new BigDecimal("9.20"));
                response.setRateLastUpdated(Instant.parse("2026-10-17T00:00:00Z"));
                response.setRateNextUpdate(Instant.parse("2026-10-18T00:00:00Z"));
                response.setRateProvider("ExchangeRate-API Open Access");
                response.setAttributionUrl("https://www.exchangerate-api.com");
                return response;
        }
}
