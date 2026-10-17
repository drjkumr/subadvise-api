package com.drjcodes.subadvise_api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.drjcodes.subadvise_api.enums.Currency;
import lombok.Data;

@Data
public class ExpenseRequestDto {
    private BigDecimal actualExpense;
    private Currency currency;
    private UUID targetId;
}
