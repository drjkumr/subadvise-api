package com.drjcodes.subadvise_api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.drjcodes.subadvise_api.enums.Currency;

class SubscriptionTest {

    @Test
    void prePersistSetsSubscriptionDateWhenMissing() {
        Subscription subscription = Subscription.builder()
                .subscriptionId(UUID.randomUUID())
                .subscriptionName("Streaming")
                .renewalDate(LocalDate.of(2025, 2, 10))
                .subscriptionPrice(new BigDecimal("12.99"))
                .currency(Currency.USD)
                .build();

        subscription.prePersist();

        assertEquals(LocalDate.now(), subscription.getSubscriptionDate());
    }

    @Test
    void prePersistPreservesExistingSubscriptionDate() {
        LocalDate subscriptionDate = LocalDate.of(2024, 12, 1);
        Subscription subscription = Subscription.builder()
                .subscriptionId(UUID.randomUUID())
                .subscriptionName("Streaming")
                .subscriptionDate(subscriptionDate)
                .renewalDate(LocalDate.of(2025, 1, 1))
                .subscriptionPrice(new BigDecimal("12.99"))
                .currency(Currency.EUR)
                .build();

        subscription.prePersist();

        assertEquals(subscriptionDate, subscription.getSubscriptionDate());
    }

    @Test
    void builderCreatesSubscriptionWithProvidedFields() {
        UUID id = UUID.randomUUID();
        LocalDate subscriptionDate = LocalDate.of(2025, 1, 10);
        LocalDate renewalDate = LocalDate.of(2025, 2, 10);
        BigDecimal price = new BigDecimal("12.99");
        Currency currency = Currency.GBP;

        Subscription subscription = Subscription.builder()
                .subscriptionId(id)
                .subscriptionName("Streaming")
                .subscriptionDate(subscriptionDate)
                .renewalDate(renewalDate)
                .subscriptionPrice(price)
                .currency(currency)
                .build();

        assertNotNull(subscription);
        assertEquals(id, subscription.getSubscriptionId());
        assertEquals("Streaming", subscription.getSubscriptionName());
        assertEquals(subscriptionDate, subscription.getSubscriptionDate());
        assertEquals(renewalDate, subscription.getRenewalDate());
        assertEquals(price, subscription.getSubscriptionPrice());
        assertEquals(currency, subscription.getCurrency());
    }
}
