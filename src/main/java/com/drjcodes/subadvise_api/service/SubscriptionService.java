package com.drjcodes.subadvise_api.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.dto.MonthlySubscriptionTotalDto;
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

    public MonthlySubscriptionTotalDto getMonthlySubscriptionTotals(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        List<Subscription> subscriptions = subscriptionRepository.findByRenewalDateBetween(
                yearMonth.atDay(1), yearMonth.atEndOfMonth());
        Map<Currency, BigDecimal> totals = new EnumMap<>(Currency.class);
        for (Subscription subscription : subscriptions) {
            totals.merge(subscription.getCurrency(), subscription.getSubscriptionPrice(), BigDecimal::add);
        }

        MonthlySubscriptionTotalDto result = new MonthlySubscriptionTotalDto();
        result.setYear(year);
        result.setMonth(month);
        result.setTotalsByCurrency(totals);
        return result;
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