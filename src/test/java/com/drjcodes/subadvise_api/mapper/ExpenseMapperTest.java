package com.drjcodes.subadvise_api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.drjcodes.subadvise_api.dto.ExpenseRequestDto;
import com.drjcodes.subadvise_api.dto.ExpenseResponseDto;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.model.Expense;
import com.drjcodes.subadvise_api.model.Target;

class ExpenseMapperTest {

    private final ExpenseMapper expenseMapper = new ExpenseMapper();

    @Test
    void mapsRequestDtoToEntityAndTargetReference() {
        UUID targetId = UUID.randomUUID();
        ExpenseRequestDto request = new ExpenseRequestDto();
        request.setActualExpense(new BigDecimal("200.00"));
        request.setCurrency(Currency.USD);
        request.setTargetId(targetId);

        Expense result = expenseMapper.toEntity(request);

        assertEquals(request.getActualExpense(), result.getActualExpense());
        assertEquals(request.getCurrency(), result.getCurrency());
        assertEquals(targetId, result.getTarget().getTargetId());
    }

    @Test
    void mapsEntityToResponseDto() {
        UUID expenseId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Expense expense = Expense.builder()
                .expenseId(expenseId)
                .actualExpense(new BigDecimal("200.00"))
                .difference(new BigDecimal("50.00"))
                .currency(Currency.EUR)
                .target(Target.builder().targetId(targetId).currency(Currency.EUR).build())
                .build();

        ExpenseResponseDto result = expenseMapper.toResponseDto(expense);

        assertEquals(expenseId, result.getExpenseId());
        assertEquals(expense.getActualExpense(), result.getActualExpense());
        assertEquals(expense.getDifference(), result.getDifference());
        assertEquals(expense.getCurrency(), result.getCurrency());
        assertEquals(targetId, result.getTargetId());
    }

    @Test
    void mapsNullTargetIdAndHandlesNullInputs() {
        ExpenseRequestDto request = new ExpenseRequestDto();

        Expense result = expenseMapper.toEntity(request);

        assertNull(result.getTarget());
        assertNull(expenseMapper.toEntity(null));
        assertNull(expenseMapper.toResponseDto(null));
    }
}
