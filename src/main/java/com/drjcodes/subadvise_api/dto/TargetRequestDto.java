package com.drjcodes.subadvise_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.drjcodes.subadvise_api.enums.Currency;
import lombok.Data;

@Data
public class TargetRequestDto {
    private BigDecimal targetExpense;
    private Currency currency;
    private LocalDate startDate;
    private LocalDate endDate;
}
