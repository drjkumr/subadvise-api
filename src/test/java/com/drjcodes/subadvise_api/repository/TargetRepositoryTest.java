package com.drjcodes.subadvise_api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.model.Target;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class TargetRepositoryTest {

    @Autowired
    private TargetRepository targetRepository;

    @Test
    void savesAndLoadsTargetAndAppliesStartDateCallback() {
        Target target = Target.builder()
                .targetExpense(new BigDecimal("250.00"))
                .currency(Currency.USD)
                .endDate(LocalDate.of(2025, 12, 31))
                .build();

        Target saved = targetRepository.saveAndFlush(target);
        Target loaded = targetRepository.findById(saved.getTargetId()).orElseThrow();

        assertNotNull(loaded.getTargetId());
        assertEquals(new BigDecimal("250.00"), loaded.getTargetExpense());
        assertEquals(Currency.USD, loaded.getCurrency());
        assertEquals(LocalDate.now(), loaded.getStartDate());
        assertEquals(LocalDate.of(2025, 12, 31), loaded.getEndDate());
    }
}
