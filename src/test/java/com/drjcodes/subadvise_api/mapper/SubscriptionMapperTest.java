package com.drjcodes.subadvise_api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.drjcodes.subadvise_api.dto.SubscriptionRequestDto;
import com.drjcodes.subadvise_api.dto.SubscriptionResponseDto;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.model.Subscription;

class SubscriptionMapperTest {

    private final SubscriptionMapper subscriptionMapper = new SubscriptionMapper();

    @Test
    void mapsRequestDtoToEntityIncludingCurrency() {
        SubscriptionRequestDto request = new SubscriptionRequestDto();
        request.setSubscriptionName("Streaming");
        request.setSubscriptionDate(LocalDate.of(2025, 1, 10));
        request.setRenewalDate(LocalDate.of(2025, 2, 10));
        request.setSubscriptionPrice(new BigDecimal("12.99"));
        request.setCurrency(Currency.EUR);

        Subscription result = subscriptionMapper.toEntity(request);

        assertEquals(request.getSubscriptionName(), result.getSubscriptionName());
        assertEquals(request.getSubscriptionDate(), result.getSubscriptionDate());
        assertEquals(request.getRenewalDate(), result.getRenewalDate());
        assertEquals(request.getSubscriptionPrice(), result.getSubscriptionPrice());
        assertEquals(request.getCurrency(), result.getCurrency());
    }

    @Test
    void mapsEntityToResponseDtoIncludingCurrency() {
        Subscription subscription = Subscription.builder()
                .subscriptionId(UUID.randomUUID())
                .subscriptionName("Streaming")
                .subscriptionDate(LocalDate.of(2025, 1, 10))
                .renewalDate(LocalDate.of(2025, 2, 10))
                .subscriptionPrice(new BigDecimal("12.99"))
                .currency(Currency.GBP)
                .build();

        SubscriptionResponseDto result = subscriptionMapper.toResponseDto(subscription);

        assertEquals(subscription.getSubscriptionId(), result.getSubscriptionId());
        assertEquals(subscription.getSubscriptionName(), result.getSubscriptionName());
        assertEquals(subscription.getSubscriptionDate(), result.getSubscriptionDate());
        assertEquals(subscription.getRenewalDate(), result.getRenewalDate());
        assertEquals(subscription.getSubscriptionPrice(), result.getSubscriptionPrice());
        assertEquals(subscription.getCurrency(), result.getCurrency());
    }

    @Test
    void returnsNullForNullInputs() {
        assertNull(subscriptionMapper.toEntity(null));
        assertNull(subscriptionMapper.toResponseDto(null));
    }
}
