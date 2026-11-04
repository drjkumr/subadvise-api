package com.drjcodes.subadvise_api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.drjcodes.subadvise_api.enums.Currency;

class TargetTest {

    @Test
    void prePersistSetsStartDateWhenMissing() {
        Target target = Target.builder()
                .targetExpense(new BigDecimal("250.00"))
                .currency(Currency.USD)
                .endDate(LocalDate.of(2025, 12, 31))
                .build();

        target.prePersist();

        assertEquals(LocalDate.now(), target.getStartDate());
    }

    @Test
    void prePersistPreservesProvidedStartDate() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        Target target = Target.builder()
                .targetExpense(new BigDecimal("250.00"))
                .currency(Currency.EUR)
                .startDate(startDate)
                .endDate(LocalDate.of(2025, 12, 31))
                .build();

        target.prePersist();

        assertEquals(startDate, target.getStartDate());
    }
}
