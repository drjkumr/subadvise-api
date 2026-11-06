package com.drjcodes.subadvise_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.drjcodes.subadvise_api.dto.MonthlySubscriptionTotalDto;
import com.drjcodes.subadvise_api.dto.SubscriptionRequestDto;
import com.drjcodes.subadvise_api.dto.SubscriptionResponseDto;
import com.drjcodes.subadvise_api.mapper.SubscriptionMapper;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.model.Subscription;
import com.drjcodes.subadvise_api.repository.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @Mock
    private SubscriptionColorService subscriptionColorService;

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionService(
                subscriptionRepository,
                subscriptionMapper,
                subscriptionColorService);
    }

    @Test
    void getAllSubscriptionsReturnsMappedResults() {
        Subscription subscription = mock(Subscription.class);
        SubscriptionResponseDto response = mock(SubscriptionResponseDto.class);
        when(subscriptionRepository.findAll()).thenReturn(List.of(subscription));
        when(subscriptionMapper.toResponseDto(subscription)).thenReturn(response);

        List<SubscriptionResponseDto> result = subscriptionService.getAllSubscriptions();

        assertEquals(List.of(response), result);
        verify(subscriptionMapper).toResponseDto(subscription);
    }

    @Test
    void getSubscriptionByIdReturnsMappedResultWhenFound() {
        UUID id = UUID.randomUUID();
        Subscription subscription = mock(Subscription.class);
        SubscriptionResponseDto response = mock(SubscriptionResponseDto.class);
        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscription));
        when(subscriptionMapper.toResponseDto(subscription)).thenReturn(response);

        Optional<SubscriptionResponseDto> result = subscriptionService.getSubscriptionById(id);

        assertEquals(Optional.of(response), result);
    }

    @Test
    void getSubscriptionByIdReturnsEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(subscriptionRepository.findById(id)).thenReturn(Optional.empty());

        Optional<SubscriptionResponseDto> result = subscriptionService.getSubscriptionById(id);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void searchSubscriptionsByNameTrimsQueryAndReturnsMappedResults() {
        Subscription subscription = mock(Subscription.class);
        SubscriptionResponseDto response = mock(SubscriptionResponseDto.class);
        when(subscriptionRepository.findBySubscriptionNameContainingIgnoreCase("Netflix")).thenReturn(List.of(subscription));
        when(subscriptionMapper.toResponseDto(subscription)).thenReturn(response);

        List<SubscriptionResponseDto> result = subscriptionService.searchSubscriptionsByName("  Netflix  ");

        assertEquals(List.of(response), result);
        verify(subscriptionRepository).findBySubscriptionNameContainingIgnoreCase("Netflix");
        verify(subscriptionMapper).toResponseDto(subscription);
    }

    @Test
    void searchSubscriptionsByNameRejectsBlankQuery() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.searchSubscriptionsByName("  "));

        assertEquals("name must not be blank", exception.getMessage());
        verify(subscriptionRepository, never()).findBySubscriptionNameContainingIgnoreCase(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void saveSubscriptionMapsAndSavesRequest() {
        SubscriptionRequestDto request = mock(SubscriptionRequestDto.class);
        Subscription subscription = mock(Subscription.class);
        Subscription savedSubscription = mock(Subscription.class);
        SubscriptionResponseDto response = mock(SubscriptionResponseDto.class);
        when(subscriptionMapper.toEntity(request)).thenReturn(subscription);
        when(subscriptionRepository.save(subscription)).thenReturn(savedSubscription);
        when(subscriptionMapper.toResponseDto(savedSubscription)).thenReturn(response);

        SubscriptionResponseDto result = subscriptionService.saveSubscription(request);

        assertSame(response, result);
        verify(subscriptionMapper).toEntity(request);
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void deleteSubscriptionDeletesById() {
        UUID id = UUID.randomUUID();

        subscriptionService.deleteSubscription(id);

        verify(subscriptionRepository).deleteById(id);
    }

    @Test
    void updateSubscriptionAppliesAllNonNullRequestFields() {
        UUID id = UUID.randomUUID();
        LocalDate subscriptionDate = LocalDate.of(2025, 1, 10);
        LocalDate renewalDate = LocalDate.of(2025, 2, 10);
        BigDecimal price = new BigDecimal("12.99");
        Currency currency = Currency.EUR;
        SubscriptionRequestDto request = mock(SubscriptionRequestDto.class);
        Subscription existing = mock(Subscription.class);
        Subscription saved = mock(Subscription.class);
        SubscriptionResponseDto response = mock(SubscriptionResponseDto.class);
        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(request.getSubscriptionName()).thenReturn("Streaming");
        when(request.getSubscriptionDate()).thenReturn(subscriptionDate);
        when(request.getRenewalDate()).thenReturn(renewalDate);
        when(request.getSubscriptionPrice()).thenReturn(price);
        when(request.getCurrency()).thenReturn(currency);
        when(subscriptionRepository.save(existing)).thenReturn(saved);
        when(subscriptionMapper.toResponseDto(saved)).thenReturn(response);

        SubscriptionResponseDto result = subscriptionService.updateSubscription(id, request);

        assertSame(response, result);
        verify(existing).setSubscriptionName("Streaming");
        verify(existing).setSubscriptionDate(subscriptionDate);
        verify(existing).setRenewalDate(renewalDate);
        verify(existing).setSubscriptionPrice(price);
        verify(existing).setCurrency(currency);
        verify(subscriptionRepository).save(existing);
    }

    @Test
    void updateSubscriptionLeavesNullRequestFieldsUnchanged() {
        UUID id = UUID.randomUUID();
        SubscriptionRequestDto request = mock(SubscriptionRequestDto.class);
        Subscription existing = mock(Subscription.class);
        Subscription saved = mock(Subscription.class);
        SubscriptionResponseDto response = mock(SubscriptionResponseDto.class);
        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(existing)).thenReturn(saved);
        when(subscriptionMapper.toResponseDto(saved)).thenReturn(response);

        subscriptionService.updateSubscription(id, request);

        verify(existing, never()).setSubscriptionName(org.mockito.ArgumentMatchers.any());
        verify(existing, never()).setSubscriptionDate(org.mockito.ArgumentMatchers.any());
        verify(existing, never()).setRenewalDate(org.mockito.ArgumentMatchers.any());
        verify(existing, never()).setSubscriptionPrice(org.mockito.ArgumentMatchers.any());
        verify(existing, never()).setCurrency(org.mockito.ArgumentMatchers.any());
        verify(subscriptionRepository).save(existing);
    }

    @Test
    void updateSubscriptionRequiresANewPriceWhenCurrencyChanges() {
        UUID id = UUID.randomUUID();
        Subscription existing = Subscription.builder()
                .subscriptionId(id)
                .subscriptionName("Streaming")
                .subscriptionPrice(new BigDecimal("12.99"))
                .currency(Currency.USD)
                .build();
        SubscriptionRequestDto request = mock(SubscriptionRequestDto.class);
        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(request.getCurrency()).thenReturn(Currency.EUR);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.updateSubscription(id, request));

        assertEquals("Provide a new subscriptionPrice or use the currency conversion endpoint",
                exception.getMessage());
        assertEquals(new BigDecimal("12.99"), existing.getSubscriptionPrice());
        assertEquals(Currency.USD, existing.getCurrency());
        verify(subscriptionRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateSubscriptionThrowsWhenIdDoesNotExist() {
        UUID id = UUID.randomUUID();
        SubscriptionRequestDto request = mock(SubscriptionRequestDto.class);
        when(subscriptionRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> subscriptionService.updateSubscription(id, request));

        assertEquals("Subscription not found with id " + id, exception.getMessage());
        verify(subscriptionRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getSubscriptionsBySubscriptionDateReturnsMappedResults() {
        LocalDate date = LocalDate.of(2025, 1, 10);
        Subscription subscription = mock(Subscription.class);
        SubscriptionResponseDto response = mock(SubscriptionResponseDto.class);
        when(subscriptionRepository.findBySubscriptionDate(date)).thenReturn(List.of(subscription));
        when(subscriptionMapper.toResponseDto(subscription)).thenReturn(response);

        List<SubscriptionResponseDto> result = subscriptionService.getSubscriptionsBySubscriptionDate(date);

        assertEquals(List.of(response), result);
    }

    @Test
    void getSubscriptionsByRenewalDateReturnsMappedResults() {
        LocalDate date = LocalDate.of(2025, 2, 10);
        Subscription subscription = mock(Subscription.class);
        SubscriptionResponseDto response = mock(SubscriptionResponseDto.class);
        when(subscriptionRepository.findByRenewalDate(date)).thenReturn(List.of(subscription));
        when(subscriptionMapper.toResponseDto(subscription)).thenReturn(response);

        List<SubscriptionResponseDto> result = subscriptionService.getSubscriptionsByRenewalDate(date);

        assertEquals(List.of(response), result);
    }

    @Test
    void getSubscriptionsByRenewalDateBetweenReturnsMappedResults() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        Subscription subscription = mock(Subscription.class);
        SubscriptionResponseDto response = mock(SubscriptionResponseDto.class);
        when(subscriptionRepository.findByRenewalDateBetween(startDate, endDate))
                .thenReturn(List.of(subscription));
        when(subscriptionMapper.toResponseDto(subscription)).thenReturn(response);

        List<SubscriptionResponseDto> result = subscriptionService.getSubscriptionsByRenewalDateBetween(startDate,
                endDate);

        assertEquals(List.of(response), result);
    }

    @Test
    void getMonthlySubscriptionTotalsSumsPricesSeparatelyByCurrency() {
        Subscription usdOne = subscription("9.99", Currency.USD);
        Subscription usdTwo = subscription("5.01", Currency.USD);
        Subscription eur = subscription("7.50", Currency.EUR);
        LocalDate startDate = LocalDate.of(2025, 2, 1);
        LocalDate endDate = LocalDate.of(2025, 2, 28);
        when(subscriptionRepository.findByRenewalDateBetween(startDate, endDate))
                .thenReturn(List.of(usdOne, usdTwo, eur));

        MonthlySubscriptionTotalDto result = subscriptionService.getMonthlySubscriptionTotals(2025, 2);

        assertEquals(2025, result.getYear());
        assertEquals(2, result.getMonth());
        assertEquals(new BigDecimal("15.00"), result.getTotalsByCurrency().get(Currency.USD));
        assertEquals(new BigDecimal("7.50"), result.getTotalsByCurrency().get(Currency.EUR));
        verify(subscriptionRepository).findByRenewalDateBetween(startDate, endDate);
    }

    @Test
    void getMonthlySubscriptionTotalsRejectsInvalidMonth() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.getMonthlySubscriptionTotals(2025, 13));

        assertEquals("month must be between 1 and 12", exception.getMessage());
        verify(subscriptionRepository, never()).findByRenewalDateBetween(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getSubscriptionsBySubscriptionDateBetweenReturnsMappedResults() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        Subscription subscription = mock(Subscription.class);
        SubscriptionResponseDto response = mock(SubscriptionResponseDto.class);
        when(subscriptionRepository.findBySubscriptionDateBetween(startDate, endDate))
                .thenReturn(List.of(subscription));
        when(subscriptionMapper.toResponseDto(subscription)).thenReturn(response);

        List<SubscriptionResponseDto> result = subscriptionService.getSubscriptionsBySubscriptionDateBetween(startDate,
                endDate);

        assertEquals(List.of(response), result);
    }

    private Subscription subscription(String price, Currency currency) {
        return Subscription.builder()
                .subscriptionPrice(new BigDecimal(price))
                .currency(currency)
                .build();
    }
}
