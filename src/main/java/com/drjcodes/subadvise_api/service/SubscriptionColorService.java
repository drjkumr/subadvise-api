package com.drjcodes.subadvise_api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.drjcodes.subadvise_api.dto.SubscriptionResponseDto;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.enums.ExpenseColor;
import com.drjcodes.subadvise_api.model.Subscription;
import com.drjcodes.subadvise_api.repository.SubscriptionRepository;

@Service
public class SubscriptionColorService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final Currency BASE_CURRENCY = Currency.USD;

    private final SubscriptionRepository subscriptionRepository;
    private final CurrencyConversionService currencyConversionService;
    private final ExpenseService expenseService;

    public SubscriptionColorService(
            SubscriptionRepository subscriptionRepository,
            CurrencyConversionService currencyConversionService,
            ExpenseService expenseService) {
        this.subscriptionRepository = subscriptionRepository;
        this.currencyConversionService = currencyConversionService;
        this.expenseService = expenseService;
    }

    public void applyColors(List<SubscriptionResponseDto> subscriptions) {
        if (subscriptions.isEmpty()) {
            return;
        }

        Map<UUID, BigDecimal> amountsInUsd = new HashMap<>();
        BigDecimal totalInUsd = ZERO;
        for (Subscription subscription : subscriptionRepository.findAll()) {
            BigDecimal amountInUsd = subscription.getCurrency() == BASE_CURRENCY
                    ? subscription.getSubscriptionPrice()
                    : currencyConversionService.convert(
                            subscription.getSubscriptionPrice(),
                            subscription.getCurrency(),
                            BASE_CURRENCY)
                            .getConvertedAmount();
            amountsInUsd.put(subscription.getSubscriptionId(), amountInUsd);
            totalInUsd = totalInUsd.add(amountInUsd);
        }

        Map<ExpenseColor, BigDecimal> thresholds = expenseService.getThresholdMap();
        for (SubscriptionResponseDto subscription : subscriptions) {
            BigDecimal amountInUsd = amountsInUsd.get(subscription.getSubscriptionId());
            if (amountInUsd == null) {
                throw new IllegalStateException(
                        "Could not calculate a color for subscription " + subscription.getSubscriptionId());
            }
            BigDecimal percentage = totalInUsd.signum() == 0
                    ? ZERO
                    : amountInUsd.multiply(HUNDRED).divide(totalInUsd, 2, RoundingMode.HALF_UP);
            subscription.setColor(expenseService.classify(percentage, thresholds));
        }
    }
}
