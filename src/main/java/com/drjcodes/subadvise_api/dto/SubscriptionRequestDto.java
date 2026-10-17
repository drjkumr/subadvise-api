package com.drjcodes.subadvise_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import com.drjcodes.subadvise_api.enums.Currency;

@Data
public class SubscriptionRequestDto {
    private String subscriptionName;
    private LocalDate subscriptionDate;
    private LocalDate renewalDate;
    private BigDecimal subscriptionPrice;
    private Currency currency;
}