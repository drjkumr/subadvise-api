package com.drjcodes.subadvise_api.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.drjcodes.subadvise_api.dto.SubscriptionRequestDto;
import com.drjcodes.subadvise_api.dto.SubscriptionResponseDto;
import com.drjcodes.subadvise_api.exception.SubscriptionNotFoundException;
import com.drjcodes.subadvise_api.mapper.SubscriptionMapper;
import com.drjcodes.subadvise_api.model.Subscription;
import com.drjcodes.subadvise_api.repository.SubscriptionRepository;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

    SubscriptionService(SubscriptionRepository subscriptionRepository, SubscriptionMapper subscriptionMapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionMapper = subscriptionMapper;
    }

    public List<SubscriptionResponseDto> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .map(subscriptionMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public Optional<SubscriptionResponseDto> getSubscriptionById(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .map(subscriptionMapper::toResponseDto);
    }

    public List<SubscriptionResponseDto> searchSubscriptionsByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        return subscriptionRepository.findBySubscriptionNameContainingIgnoreCase(name.trim()).stream()
                .map(subscriptionMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public SubscriptionResponseDto saveSubscription(SubscriptionRequestDto requestDto) {
        Subscription subscription = subscriptionMapper.toEntity(requestDto);
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toResponseDto(savedSubscription);
    }

    public void deleteSubscription(UUID id) {
        subscriptionRepository.deleteById(id);
    }

    public SubscriptionResponseDto updateSubscription(UUID id, SubscriptionRequestDto updatedDto) {
        Subscription existingSubscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException(id));

        boolean currencyChanges = updatedDto.getCurrency() != null
                && !updatedDto.getCurrency().equals(existingSubscription.getCurrency());
        if (currencyChanges && updatedDto.getSubscriptionPrice() == null) {
            throw new IllegalArgumentException(
                    "Provide a new subscriptionPrice or use the currency conversion endpoint");
        }

        if (updatedDto.getSubscriptionName() != null) {
            existingSubscription.setSubscriptionName(updatedDto.getSubscriptionName());
        }
        if (updatedDto.getSubscriptionDate() != null) {
            existingSubscription.setSubscriptionDate(updatedDto.getSubscriptionDate());
        }
        if (updatedDto.getRenewalDate() != null) {
            existingSubscription.setRenewalDate(updatedDto.getRenewalDate());
        }
        if (updatedDto.getSubscriptionPrice() != null) {
            existingSubscription.setSubscriptionPrice(updatedDto.getSubscriptionPrice());
        }
        if (updatedDto.getCurrency() != null) {
            existingSubscription.setCurrency(updatedDto.getCurrency());
        }

        Subscription savedSubscription = subscriptionRepository.save(existingSubscription);
        return subscriptionMapper.toResponseDto(savedSubscription);
    }

    public List<SubscriptionResponseDto> getSubscriptionsBySubscriptionDate(LocalDate subscriptionDate) {
        return subscriptionRepository.findBySubscriptionDate(subscriptionDate).stream()
                .map(subscriptionMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<SubscriptionResponseDto> getSubscriptionsByRenewalDate(LocalDate renewalDate) {
        return subscriptionRepository.findByRenewalDate(renewalDate).stream()
                .map(subscriptionMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<SubscriptionResponseDto> getSubscriptionsByRenewalDateBetween(LocalDate startDate, LocalDate endDate) {
        return subscriptionRepository.findByRenewalDateBetween(startDate, endDate).stream()
                .map(subscriptionMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<SubscriptionResponseDto> getSubscriptionsBySubscriptionDateBetween(LocalDate startDate,
            LocalDate endDate) {
        return subscriptionRepository.findBySubscriptionDateBetween(startDate, endDate).stream()
                .map(subscriptionMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}