package com.drjcodes.subadvise_api.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.drjcodes.subadvise_api.dto.CurrencyConversionResponseDto;
import com.drjcodes.subadvise_api.dto.SubscriptionCurrencyConversionResponseDto;
import com.drjcodes.subadvise_api.mapper.SubscriptionMapper;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.exception.SubscriptionNotFoundException;
import com.drjcodes.subadvise_api.model.Subscription;
import com.drjcodes.subadvise_api.repository.SubscriptionRepository;

@Service
public class SubscriptionCurrencyService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final CurrencyConversionService currencyConversionService;

    public SubscriptionCurrencyService(
            SubscriptionRepository subscriptionRepository,
            SubscriptionMapper subscriptionMapper,
            CurrencyConversionService currencyConversionService) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionMapper = subscriptionMapper;
        this.currencyConversionService = currencyConversionService;
    }

    public SubscriptionCurrencyConversionResponseDto convertSubscriptionCurrency(UUID subscriptionId, Currency to) {
        Subscription existingSubscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId));

        CurrencyConversionResponseDto conversion = currencyConversionService.convert(
                existingSubscription.getSubscriptionPrice(), existingSubscription.getCurrency(), to);

        existingSubscription.setSubscriptionPrice(conversion.getConvertedAmount());
        existingSubscription.setCurrency(to);

        Subscription savedSubscription = subscriptionRepository.save(existingSubscription);
        SubscriptionCurrencyConversionResponseDto response = new SubscriptionCurrencyConversionResponseDto();
        response.setSubscription(subscriptionMapper.toResponseDto(savedSubscription));
        response.setConversion(conversion);
        return response;
    }
}