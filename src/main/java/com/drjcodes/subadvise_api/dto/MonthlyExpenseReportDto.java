package com.drjcodes.subadvise_api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.drjcodes.subadvise_api.enums.Currency;
import lombok.Data;

@Data
public class MonthlyExpenseReportDto {
    private int year;
    private int month;
    private Map<Currency, BigDecimal> totalsByCurrency;
    private List<MonthlyExpenseItemDto> expenses;
}