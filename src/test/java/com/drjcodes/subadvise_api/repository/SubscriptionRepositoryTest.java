package com.drjcodes.subadvise_api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.model.Subscription;

@DataJpaTest(properties = {
                "spring.flyway.enabled=false",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class SubscriptionRepositoryTest {

        @Autowired
        private SubscriptionRepository subscriptionRepository;

        @Test
        void findsSubscriptionsBySubscriptionDateAndRenewalDate() {
                LocalDate subscriptionDate = LocalDate.of(2025, 1, 10);
                LocalDate renewalDate = LocalDate.of(2025, 2, 10);
                Subscription matching = subscriptionRepository.saveAndFlush(
                                subscription("Matching", subscriptionDate, renewalDate));
                subscriptionRepository.saveAndFlush(subscription(
                                "Other date", LocalDate.of(2025, 1, 11), LocalDate.of(2025, 2, 11)));

                List<Subscription> bySubscriptionDate = subscriptionRepository.findBySubscriptionDate(subscriptionDate);
                List<Subscription> byRenewalDate = subscriptionRepository.findByRenewalDate(renewalDate);

                assertEquals(1, bySubscriptionDate.size());
                assertEquals(matching.getSubscriptionId(), bySubscriptionDate.get(0).getSubscriptionId());
                assertEquals(Currency.USD, bySubscriptionDate.get(0).getCurrency());
                assertEquals(1, byRenewalDate.size());
                assertEquals(matching.getSubscriptionId(), byRenewalDate.get(0).getSubscriptionId());
        }

        @Test
        void findsSubscriptionsWithinInclusiveDateRanges() {
                Subscription beforeRange = subscriptionRepository.saveAndFlush(subscription(
                                "Before", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1)));
                Subscription startBoundary = subscriptionRepository.saveAndFlush(subscription(
                                "Start boundary", LocalDate.of(2025, 1, 10), LocalDate.of(2025, 2, 10)));
                Subscription endBoundary = subscriptionRepository.saveAndFlush(subscription(
                                "End boundary", LocalDate.of(2025, 1, 20), LocalDate.of(2025, 2, 20)));
                subscriptionRepository.saveAndFlush(subscription(
                                "After range", LocalDate.of(2025, 1, 21), LocalDate.of(2025, 2, 21)));

                List<Subscription> bySubscriptionDate = subscriptionRepository.findBySubscriptionDateBetween(
                                LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 20));
                List<Subscription> byRenewalDate = subscriptionRepository.findByRenewalDateBetween(
                                LocalDate.of(2025, 2, 10), LocalDate.of(2025, 2, 20));

                assertEquals(2, bySubscriptionDate.size());
                assertTrue(bySubscriptionDate.stream()
                                .anyMatch(item -> item.getSubscriptionId().equals(startBoundary.getSubscriptionId())));
                assertTrue(bySubscriptionDate.stream()
                                .anyMatch(item -> item.getSubscriptionId().equals(endBoundary.getSubscriptionId())));
                assertEquals(2, byRenewalDate.size());
                assertTrue(byRenewalDate.stream()
                                .noneMatch(item -> item.getSubscriptionId().equals(beforeRange.getSubscriptionId())));
                assertTrue(byRenewalDate.stream()
                                .anyMatch(item -> item.getSubscriptionId().equals(startBoundary.getSubscriptionId())));
                assertTrue(byRenewalDate.stream()
                                .anyMatch(item -> item.getSubscriptionId().equals(endBoundary.getSubscriptionId())));
        }

        private Subscription subscription(String name, LocalDate subscriptionDate, LocalDate renewalDate) {
                return Subscription.builder()
                                .subscriptionName(name)
                                .subscriptionDate(subscriptionDate)
                                .renewalDate(renewalDate)
                                .subscriptionPrice(new BigDecimal("9.99"))
                                .currency(Currency.USD)
                                .build();
        }
}
