package com.drjcodes.subadvise_api.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.drjcodes.subadvise_api.dto.SubscriptionRequestDto;
import com.drjcodes.subadvise_api.dto.SubscriptionCurrencyConversionResponseDto;
import com.drjcodes.subadvise_api.dto.SubscriptionResponseDto;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.service.SubscriptionCurrencyService;
import com.drjcodes.subadvise_api.service.SubscriptionService;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionCurrencyService subscriptionCurrencyService;

    public SubscriptionController(
            SubscriptionService subscriptionService,
            SubscriptionCurrencyService subscriptionCurrencyService) {
        this.subscriptionService = subscriptionService;
        this.subscriptionCurrencyService = subscriptionCurrencyService;
    }

    @GetMapping
    public List<SubscriptionResponseDto> getAllSubscriptions() {
        return subscriptionService.getAllSubscriptions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDto> getSubscriptionById(@PathVariable UUID id) {
        return subscriptionService.getSubscriptionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public SubscriptionResponseDto createSubscription(@RequestBody SubscriptionRequestDto requestDto) {
        return subscriptionService.saveSubscription(requestDto);
    }

    @PostMapping("/{id}/convert")
    public SubscriptionCurrencyConversionResponseDto convertSubscriptionCurrency(
            @PathVariable UUID id,
            @RequestParam Currency to) {
        return subscriptionCurrencyService.convertSubscriptionCurrency(id, to);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDto> updateSubscription(
            @PathVariable UUID id,
            @RequestBody SubscriptionRequestDto requestDto) {
        SubscriptionResponseDto updated = subscriptionService.updateSubscription(id, requestDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable UUID id) {
        try {
            subscriptionService.deleteSubscription(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/filter/subscription-date")
    public List<SubscriptionResponseDto> getBySubscriptionDate(@RequestParam LocalDate date) {
        return subscriptionService.getSubscriptionsBySubscriptionDate(date);
    }

    @GetMapping("/filter/renewal-date")
    public List<SubscriptionResponseDto> getByRenewalDate(@RequestParam LocalDate date) {
        return subscriptionService.getSubscriptionsByRenewalDate(date);
    }

    @GetMapping("/filter/renewal-range")
    public List<SubscriptionResponseDto> getByRenewalDateBetween(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return subscriptionService.getSubscriptionsByRenewalDateBetween(startDate, endDate);
    }

    @GetMapping("/filter/subscription-range")
    public List<SubscriptionResponseDto> getBySubscriptionDateBetween(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return subscriptionService.getSubscriptionsBySubscriptionDateBetween(startDate, endDate);
    }
}