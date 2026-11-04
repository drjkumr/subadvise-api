package com.drjcodes.subadvise_api.mapper;

import com.drjcodes.subadvise_api.dto.SubscriptionRequestDto;
import com.drjcodes.subadvise_api.dto.SubscriptionResponseDto;
import com.drjcodes.subadvise_api.model.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public Subscription toEntity(SubscriptionRequestDto dto) {
        if (dto == null) {
            return null;
        }
        Subscription subscription = new Subscription();
        subscription.setSubscriptionName(dto.getSubscriptionName());
        subscription.setSubscriptionDate(dto.getSubscriptionDate());
        subscription.setRenewalDate(dto.getRenewalDate());
        subscription.setSubscriptionPrice(dto.getSubscriptionPrice());
        subscription.setCurrency(dto.getCurrency());
        return subscription;
    }

    public SubscriptionResponseDto toResponseDto(Subscription subscription) {
        if (subscription == null) {
            return null;
        }
        SubscriptionResponseDto dto = new SubscriptionResponseDto();
        dto.setSubscriptionId(subscription.getSubscriptionId());
        dto.setSubscriptionName(subscription.getSubscriptionName());
        dto.setSubscriptionDate(subscription.getSubscriptionDate());
        dto.setRenewalDate(subscription.getRenewalDate());
        dto.setSubscriptionPrice(subscription.getSubscriptionPrice());
        dto.setCurrency(subscription.getCurrency());
        return dto;
    }
}