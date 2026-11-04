package com.drjcodes.subadvise_api.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ExpenseColorThresholdsDto {
    private BigDecimal red;
    private BigDecimal orange;
    private BigDecimal yellow;
    private BigDecimal lime;
    private BigDecimal green;
}