package com.drjcodes.subadvise_api.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.model.Expense;
import com.drjcodes.subadvise_api.model.Subscription;
import com.drjcodes.subadvise_api.model.Target;
import com.drjcodes.subadvise_api.repository.ExpenseRepository;
import com.drjcodes.subadvise_api.repository.SubscriptionRepository;
import com.drjcodes.subadvise_api.repository.TargetRepository;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(DatabasePurgeService.class)
class DatabasePurgeServiceTest {

    @Autowired
    private DatabasePurgeService databasePurgeService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private TargetRepository targetRepository;

    @Test
    void purgesSubscriptionsWithoutAffectingTargetsOrExpenses() {
        subscriptionRepository.saveAndFlush(Subscription.builder()
                .subscriptionName("Streaming")
                .subscriptionPrice(new BigDecimal("9.99"))
                .currency(Currency.USD)
                .build());
        Target target = saveTargetWithExpense();

        databasePurgeService.purgeSubscriptions();

        assertTrue(subscriptionRepository.findAll().isEmpty());
        assertTrue(targetRepository.findById(target.getTargetId()).isPresent());
        assertTrue(expenseRepository.findAll().size() == 1);
    }

    @Test
    void purgesTargetsAndExpensesWithoutAffectingSubscriptions() {
        subscriptionRepository.saveAndFlush(Subscription.builder()
                .subscriptionName("Streaming")
                .subscriptionPrice(new BigDecimal("9.99"))
                .currency(Currency.USD)
                .build());
        saveTargetWithExpense();

        databasePurgeService.purgeTargets();

        assertTrue(expenseRepository.findAll().isEmpty());
        assertTrue(targetRepository.findAll().isEmpty());
        assertTrue(subscriptionRepository.findAll().size() == 1);
    }

    @Test
    void purgesSubscriptionsTargetsAndExpensesThatReferenceTargets() {
        subscriptionRepository.saveAndFlush(Subscription.builder()
                .subscriptionName("Streaming")
                .subscriptionPrice(new BigDecimal("9.99"))
                .currency(Currency.USD)
                .build());
        saveTargetWithExpense();

        databasePurgeService.purgeSubscriptionsAndTargets();

        assertTrue(expenseRepository.findAll().isEmpty());
        assertTrue(targetRepository.findAll().isEmpty());
        assertTrue(subscriptionRepository.findAll().isEmpty());
    }

    private Target saveTargetWithExpense() {
        Target target = targetRepository.saveAndFlush(Target.builder()
                .targetExpense(new BigDecimal("250.00"))
                .currency(Currency.USD)
                .endDate(LocalDate.of(2026, 12, 31))
                .build());

        expenseRepository.saveAndFlush(Expense.builder()
                .actualExpense(new BigDecimal("200.00"))
                .currency(Currency.USD)
                .target(target)
                .build());
        return target;
    }
}
