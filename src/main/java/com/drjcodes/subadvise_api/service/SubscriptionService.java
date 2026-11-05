package com.drjcodes.subadvise_api.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final SubscriptionColorService subscriptionColorService;

    SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            SubscriptionMapper subscriptionMapper,
            SubscriptionColorService subscriptionColorService) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionMapper = subscriptionMapper;
        this.subscriptionColorService = subscriptionColorService;
    }

    public List<SubscriptionResponseDto> getAllSubscriptions() {
        return toResponseDtos(subscriptionRepository.findAll());
    }

    public Optional<SubscriptionResponseDto> getSubscriptionById(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .map(this::toResponseDto);
    }

    public List<SubscriptionResponseDto> searchSubscriptionsByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        return toResponseDtos(subscriptionRepository.findBySubscriptionNameContainingIgnoreCase(name.trim()));
    }

    @Transactional
    public SubscriptionResponseDto saveSubscription(SubscriptionRequestDto requestDto) {
        Subscription subscription = subscriptionMapper.toEntity(requestDto);
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        return toResponseDto(savedSubscription);
    }

    public void deleteSubscription(UUID id) {
        subscriptionRepository.deleteById(id);
    }

    @Transactional
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
        return toResponseDto(savedSubscription);
    }

    public List<SubscriptionResponseDto> getSubscriptionsBySubscriptionDate(LocalDate subscriptionDate) {
        return toResponseDtos(subscriptionRepository.findBySubscriptionDate(subscriptionDate));
    }

    public List<SubscriptionResponseDto> getSubscriptionsByRenewalDate(LocalDate renewalDate) {
        return toResponseDtos(subscriptionRepository.findByRenewalDate(renewalDate));
    }

    public List<SubscriptionResponseDto> getSubscriptionsByRenewalDateBetween(LocalDate startDate, LocalDate endDate) {
        return toResponseDtos(subscriptionRepository.findByRenewalDateBetween(startDate, endDate));
    }

    public List<SubscriptionResponseDto> getSubscriptionsBySubscriptionDateBetween(LocalDate startDate,
            LocalDate endDate) {
        return toResponseDtos(subscriptionRepository.findBySubscriptionDateBetween(startDate, endDate));
    }

    private List<SubscriptionResponseDto> toResponseDtos(List<Subscription> subscriptions) {
        List<SubscriptionResponseDto> responseDtos = new ArrayList<>(subscriptions.size());
        subscriptions.stream()
                .map(subscriptionMapper::toResponseDto)
                .forEach(responseDtos::add);
        subscriptionColorService.applyColors(responseDtos);
        return responseDtos;
    }

    private SubscriptionResponseDto toResponseDto(Subscription subscription) {
        SubscriptionResponseDto responseDto = subscriptionMapper.toResponseDto(subscription);
        subscriptionColorService.applyColors(List.of(responseDto));
        return responseDto;
    }
}