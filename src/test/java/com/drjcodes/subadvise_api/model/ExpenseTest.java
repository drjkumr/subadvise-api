package com.drjcodes.subadvise_api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.drjcodes.subadvise_api.enums.Currency;

class ExpenseTest {

    @Test
    void calculateDifferenceStoresActualMinusTarget() {
        Expense expense = expense(new BigDecimal("200.00"), new BigDecimal("250.00"));

        expense.calculateDifference();

        assertEquals(new BigDecimal("-50.00"), expense.getDifference());
    }

    @Test
    void calculateDifferenceCanBeNegativeWhenActualExceedsTarget() {
        Expense expense = expense(new BigDecimal("300.00"), new BigDecimal("250.00"));

        expense.calculateDifference();

        assertEquals(new BigDecimal("50.00"), expense.getDifference());
    }

    @Test
    void calculateDifferenceLeavesValueUntouchedWhenInputsAreMissing() {
        Expense expense = Expense.builder()
                .actualExpense(new BigDecimal("200.00"))
                .difference(new BigDecimal("10.00"))
                .currency(Currency.USD)
                .target(Target.builder().targetExpense(null).currency(Currency.USD).build())
                .build();

        expense.calculateDifference();

        assertEquals(new BigDecimal("10.00"), expense.getDifference());
        assertNull(Expense.builder().build().getDifference());
    }

    @Test
    void calculateDifferenceRejectsDifferentCurrencies() {
        Expense expense = Expense.builder()
                .actualExpense(new BigDecimal("200.00"))
                .currency(Currency.EUR)
                .target(Target.builder()
                        .targetExpense(new BigDecimal("250.00"))
                        .currency(Currency.USD)
                        .build())
                .build();

        assertThrows(IllegalStateException.class, expense::calculateDifference);
    }

    private Expense expense(BigDecimal actualExpense, BigDecimal targetExpense) {
        return Expense.builder()
                .actualExpense(actualExpense)
                .currency(Currency.USD)
                .target(Target.builder()
                        .targetExpense(targetExpense)
                        .currency(Currency.USD)
                        .endDate(LocalDate.of(2025, 12, 31))
                        .build())
                .build();
    }
}
