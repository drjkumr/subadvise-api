package com.drjcodes.subadvise_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.drjcodes.subadvise_api.dto.SubscriptionResponseDto;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.enums.ExpenseColor;
import com.drjcodes.subadvise_api.mapper.ExpenseMapper;
import com.drjcodes.subadvise_api.mapper.TargetMapper;
import com.drjcodes.subadvise_api.model.Subscription;
import com.drjcodes.subadvise_api.repository.ExpenseColorThresholdRepository;
import com.drjcodes.subadvise_api.repository.ExpenseRepository;
import com.drjcodes.subadvise_api.repository.SubscriptionRepository;
import com.drjcodes.subadvise_api.repository.TargetRepository;
import com.drjcodes.subadvise_api.service.CurrencyRateProvider.RateSnapshot;

class SubscriptionColorServiceTest {

    @Test
    void appliesColorsUsingEachSubscriptionsShareOfUsdConvertedTotal() {
        Subscription usdSubscription = subscription("60.00", Currency.USD);
        Subscription eurSubscription = subscription("40.00", Currency.EUR);
        SubscriptionRepository subscriptionRepository = Mockito.mock(SubscriptionRepository.class);
        when(subscriptionRepository.findAll()).thenReturn(List.of(usdSubscription, eurSubscription));

        ExpenseColorThresholdRepository thresholdRepository = Mockito.mock(ExpenseColorThresholdRepository.class);
        when(thresholdRepository.findAll()).thenReturn(List.of());
        ExpenseService expenseService = new ExpenseService(
                Mockito.mock(ExpenseRepository.class),
                Mockito.mock(TargetRepository.class),
                Mockito.mock(ExpenseMapper.class),
                Mockito.mock(TargetMapper.class),
                thresholdRepository);

        CurrencyRateProvider rateProvider = Mockito.mock(CurrencyRateProvider.class);
        when(rateProvider.fetchLatestUsdRates()).thenReturn(new RateSnapshot(
                Map.of("USD", BigDecimal.ONE, "EUR", new BigDecimal("2")),
                Instant.now(),
                Instant.now().plusSeconds(3600)));
        CurrencyConversionService currencyConversionService = new CurrencyConversionService(rateProvider);
        SubscriptionColorService subscriptionColorService = new SubscriptionColorService(
                subscriptionRepository,
                currencyConversionService,
                expenseService);

        SubscriptionResponseDto usdResponse = responseFor(usdSubscription);
        SubscriptionResponseDto eurResponse = responseFor(eurSubscription);
        subscriptionColorService.applyColors(List.of(usdResponse, eurResponse));

        assertEquals(ExpenseColor.RED, usdResponse.getColor());
        assertEquals(ExpenseColor.YELLOW, eurResponse.getColor());
        verify(rateProvider).fetchLatestUsdRates();
    }

    private Subscription subscription(String price, Currency currency) {
        return Subscription.builder()
                .subscriptionId(UUID.randomUUID())
                .subscriptionName("Subscription")
                .subscriptionPrice(new BigDecimal(price))
                .currency(currency)
                .build();
    }

    private SubscriptionResponseDto responseFor(Subscription subscription) {
        SubscriptionResponseDto response = new SubscriptionResponseDto();
        response.setSubscriptionId(subscription.getSubscriptionId());
        return response;
    }
}
