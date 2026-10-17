package com.drjcodes.subadvise_api.mapper;

import org.springframework.stereotype.Component;

import com.drjcodes.subadvise_api.dto.ExpenseRequestDto;
import com.drjcodes.subadvise_api.dto.ExpenseResponseDto;
import com.drjcodes.subadvise_api.model.Expense;
import com.drjcodes.subadvise_api.model.Target;

@Component
public class ExpenseMapper {

    public Expense toEntity(ExpenseRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Expense expense = new Expense();
        expense.setActualExpense(dto.getActualExpense());
        expense.setCurrency(dto.getCurrency());
        if (dto.getTargetId() != null) {
            Target target = new Target();
            target.setTargetId(dto.getTargetId());
            expense.setTarget(target);
        }
        return expense;
    }

    public ExpenseResponseDto toResponseDto(Expense expense) {
        if (expense == null) {
            return null;
        }

        ExpenseResponseDto dto = new ExpenseResponseDto();
        dto.setExpenseId(expense.getExpenseId());
        dto.setActualExpense(expense.getActualExpense());
        dto.setDifference(expense.getDifference());
        dto.setCurrency(expense.getCurrency());
        if (expense.getTarget() != null) {
            dto.setTargetId(expense.getTarget().getTargetId());
        }
        return dto;
    }
}
