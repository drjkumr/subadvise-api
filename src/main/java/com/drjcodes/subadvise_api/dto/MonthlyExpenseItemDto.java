package com.drjcodes.subadvise_api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.enums.ExpenseColor;
import lombok.Data;

@Data
public class MonthlyExpenseItemDto {
    private UUID expenseId;
    private UUID targetId;
    private BigDecimal actualExpense;
    private Currency currency;
    private BigDecimal percentageOfTotal;
    private ExpenseColor color;
}