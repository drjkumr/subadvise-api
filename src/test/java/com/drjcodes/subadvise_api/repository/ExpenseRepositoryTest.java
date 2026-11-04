package com.drjcodes.subadvise_api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.model.Expense;
import com.drjcodes.subadvise_api.model.Target;

@DataJpaTest(properties = {
                "spring.flyway.enabled=false",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class ExpenseRepositoryTest {

        @Autowired
        private ExpenseRepository expenseRepository;

        @Autowired
        private TargetRepository targetRepository;

        @Test
        void savesExpenseWithTargetAndCalculatesDifference() {
                Target target = targetRepository.saveAndFlush(Target.builder()
                                .targetExpense(new BigDecimal("250.00"))
                                .currency(Currency.USD)
                                .startDate(LocalDate.of(2025, 1, 1))
                                .endDate(LocalDate.of(2025, 12, 31))
                                .build());
                Expense expense = Expense.builder()
                                .actualExpense(new BigDecimal("200.00"))
                                .currency(Currency.USD)
                                .target(target)
                                .build();

                Expense saved = expenseRepository.saveAndFlush(expense);
                Expense loaded = expenseRepository.findById(saved.getExpenseId()).orElseThrow();

                assertNotNull(loaded.getExpenseId());
                assertEquals(new BigDecimal("200.00"), loaded.getActualExpense());
                assertEquals(new BigDecimal("-50.00"), loaded.getDifference());
                assertEquals(Currency.USD, loaded.getCurrency());
                assertEquals(target.getTargetId(), loaded.getTarget().getTargetId());
        }

        @Test
        void findsMonthlyExpensesIncludingTheLastDayOfLeapFebruary() {
                saveExpenseWithEndDate(LocalDate.of(2024, 1, 31));
                saveExpenseWithEndDate(LocalDate.of(2024, 2, 1));
                saveExpenseWithEndDate(LocalDate.of(2024, 2, 29));
                saveExpenseWithEndDate(LocalDate.of(2024, 3, 1));

                var expenses = expenseRepository.findByTarget_EndDateBetween(
                                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29));

                assertEquals(2, expenses.size());
        }

        private void saveExpenseWithEndDate(LocalDate endDate) {
                Target target = targetRepository.saveAndFlush(Target.builder()
                                .targetExpense(new BigDecimal("100.00"))
                                .currency(Currency.USD)
                                .startDate(endDate.minusDays(30))
                                .endDate(endDate)
                                .build());
                expenseRepository.saveAndFlush(Expense.builder()
                                .actualExpense(new BigDecimal("20.00"))
                                .currency(Currency.USD)
                                .target(target)
                                .build());
        }
}
