package com.drjcodes.subadvise_api.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.drjcodes.subadvise_api.enums.Currency;

import lombok.Data;

@Data
public class CurrencyConversionResponseDto {
    private BigDecimal amount;
    private Currency from;
    private Currency to;
    private BigDecimal convertedAmount;
    private Instant rateLastUpdated;
    private Instant rateNextUpdate;
    private String rateProvider;
    private String attributionUrl;
}