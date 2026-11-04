package com.drjcodes.subadvise_api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.drjcodes.subadvise_api.dto.TargetRequestDto;
import com.drjcodes.subadvise_api.dto.TargetResponseDto;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.model.Target;

class TargetMapperTest {

    private final TargetMapper targetMapper = new TargetMapper();

    @Test
    void mapsRequestDtoToEntity() {
        TargetRequestDto request = new TargetRequestDto();
        request.setTargetExpense(new BigDecimal("250.00"));
        request.setCurrency(Currency.USD);
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2025, 12, 31));

        Target result = targetMapper.toEntity(request);

        assertEquals(request.getTargetExpense(), result.getTargetExpense());
        assertEquals(request.getCurrency(), result.getCurrency());
        assertEquals(request.getStartDate(), result.getStartDate());
        assertEquals(request.getEndDate(), result.getEndDate());
        org.junit.jupiter.api.Assertions.assertNull(result.getTargetId());
    }

    @Test
    void mapsEntityToResponseDto() {
        UUID id = UUID.randomUUID();
        Target target = Target.builder()
                .targetId(id)
                .targetExpense(new BigDecimal("250.00"))
                .currency(Currency.EUR)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .build();

        TargetResponseDto result = targetMapper.toResponseDto(target);

        assertEquals(id, result.getTargetId());
        assertEquals(target.getTargetExpense(), result.getTargetExpense());
        assertEquals(target.getCurrency(), result.getCurrency());
        assertEquals(target.getStartDate(), result.getStartDate());
        assertEquals(target.getEndDate(), result.getEndDate());
    }

    @Test
    void returnsNullForNullInputs() {
        assertNull(targetMapper.toEntity(null));
        assertNull(targetMapper.toResponseDto(null));
    }
}
