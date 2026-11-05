package com.drjcodes.subadvise_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.enums.ExpenseColor;

@Data
public class SubscriptionResponseDto {
    private UUID subscriptionId;
    private String subscriptionName;
    private LocalDate subscriptionDate;
    private LocalDate renewalDate;
    private BigDecimal subscriptionPrice;
    private Currency currency;
    private ExpenseColor color;
}