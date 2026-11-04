package com.drjcodes.subadvise_api.dto;

import lombok.Data;

@Data
public class SubscriptionCurrencyConversionResponseDto {
    private SubscriptionResponseDto subscription;
    private CurrencyConversionResponseDto conversion;
}