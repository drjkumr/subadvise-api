package com.drjcodes.subadvise_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.drjcodes.subadvise_api.dto.SubscriptionRequestDto;
import com.drjcodes.subadvise_api.dto.CurrencyConversionResponseDto;
import com.drjcodes.subadvise_api.dto.MonthlySubscriptionTotalDto;
import com.drjcodes.subadvise_api.dto.SubscriptionCurrencyConversionResponseDto;
import com.drjcodes.subadvise_api.dto.SubscriptionResponseDto;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.service.SubscriptionCurrencyService;
import com.drjcodes.subadvise_api.exception.SubscriptionNotFoundException;
import com.drjcodes.subadvise_api.service.SubscriptionService;

class SubAdviseControllerTest {

        private SubscriptionService subscriptionService;
        private SubscriptionCurrencyService subscriptionCurrencyService;
        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
                subscriptionService = org.mockito.Mockito.mock(SubscriptionService.class);
                subscriptionCurrencyService = org.mockito.Mockito.mock(SubscriptionCurrencyService.class);
                mockMvc = MockMvcBuilders.standaloneSetup(
                                new SubscriptionController(subscriptionService, subscriptionCurrencyService))
                                .setControllerAdvice(new ApiExceptionHandler())
                                .build();
        }

        @Test
        void getsAllSubscriptions() throws Exception {
                when(subscriptionService.getAllSubscriptions()).thenReturn(List.of(response()));

                mockMvc.perform(get("/api/subscriptions"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].subscriptionName").value("Streaming"));

                verify(subscriptionService).getAllSubscriptions();
        }

        @Test
        void searchesSubscriptionsByName() throws Exception {
                when(subscriptionService.searchSubscriptionsByName("Netflix")).thenReturn(List.of(response()));

                mockMvc.perform(get("/api/subscriptions/search").param("name", "Netflix"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].subscriptionName").value("Streaming"));

                verify(subscriptionService).searchSubscriptionsByName("Netflix");
        }

        @Test
        void getsMonthlySubscriptionTotals() throws Exception {
                MonthlySubscriptionTotalDto totals = new MonthlySubscriptionTotalDto();
                totals.setYear(2025);
                totals.setMonth(2);
                totals.setTotalsByCurrency(java.util.Map.of(
                                Currency.USD, new BigDecimal("15.00"),
                                Currency.EUR, new BigDecimal("7.50")));
                when(subscriptionService.getMonthlySubscriptionTotals(2025, 2)).thenReturn(totals);

                mockMvc.perform(get("/api/subscriptions/monthly")
                                .param("year", "2025")
                                .param("month", "2"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.year").value(2025))
                                .andExpect(jsonPath("$.month").value(2))
                                .andExpect(jsonPath("$.totalsByCurrency.USD").value(15.00))
                                .andExpect(jsonPath("$.totalsByCurrency.EUR").value(7.50));

                verify(subscriptionService).getMonthlySubscriptionTotals(2025, 2);
        }

        @Test
        void rejectsBlankSubscriptionSearchName() throws Exception {
                when(subscriptionService.searchSubscriptionsByName(" "))
                                .thenThrow(new IllegalArgumentException("name must not be blank"));

                mockMvc.perform(get("/api/subscriptions/search").param("name", " "))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("name must not be blank"));
        }

        @Test
        void getsSubscriptionByIdOrReturnsNotFound() throws Exception {
                UUID id = UUID.randomUUID();
                when(subscriptionService.getSubscriptionById(id)).thenReturn(Optional.of(response()));

                mockMvc.perform(get("/api/subscriptions/{id}", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.subscriptionName").value("Streaming"));

                when(subscriptionService.getSubscriptionById(id)).thenReturn(Optional.empty());
                mockMvc.perform(get("/api/subscriptions/{id}", id))
                                .andExpect(status().isNotFound());
        }

        @Test
        void createsSubscriptionFromJsonRequest() throws Exception {
                SubscriptionResponseDto response = response();
                when(subscriptionService.saveSubscription(any(SubscriptionRequestDto.class))).thenReturn(response);
                String body = """
                                {"subscriptionName":"Streaming","subscriptionDate":"2025-01-10",\
                                "renewalDate":"2025-02-10","subscriptionPrice":12.99,"currency":"USD"}
                                """;

                mockMvc.perform(post("/api/subscriptions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.subscriptionName").value("Streaming"))
                                .andExpect(jsonPath("$.currency").value("USD"));

                ArgumentCaptor<SubscriptionRequestDto> captor = ArgumentCaptor.forClass(SubscriptionRequestDto.class);
                verify(subscriptionService).saveSubscription(captor.capture());
                org.junit.jupiter.api.Assertions.assertEquals("Streaming", captor.getValue().getSubscriptionName());
                org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("12.99"),
                                captor.getValue().getSubscriptionPrice());
                org.junit.jupiter.api.Assertions.assertEquals(Currency.USD, captor.getValue().getCurrency());
        }

        @Test
        void convertsAndPersistsSubscriptionCurrencyOrReturnsNotFound() throws Exception {
                UUID id = UUID.randomUUID();
                SubscriptionCurrencyConversionResponseDto response = new SubscriptionCurrencyConversionResponseDto();
                response.setSubscription(response());
                response.getSubscription().setSubscriptionPrice(new BigDecimal("9.20"));
                response.getSubscription().setCurrency(Currency.EUR);

                CurrencyConversionResponseDto conversion = new CurrencyConversionResponseDto();
                conversion.setAmount(new BigDecimal("10.00"));
                conversion.setFrom(Currency.USD);
                conversion.setTo(Currency.EUR);
                conversion.setConvertedAmount(new BigDecimal("9.20"));
                conversion.setRateProvider("ExchangeRate-API Open Access");
                conversion.setAttributionUrl("https://www.exchangerate-api.com");
                response.setConversion(conversion);

                when(subscriptionCurrencyService.convertSubscriptionCurrency(id, Currency.EUR))
                                .thenReturn(response)
                                .thenThrow(new SubscriptionNotFoundException(id));

                mockMvc.perform(patch("/api/subscriptions/{id}/convert", id).param("to", "EUR"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.subscription.subscriptionPrice").value(9.20))
                                .andExpect(jsonPath("$.subscription.currency").value("EUR"))
                                .andExpect(jsonPath("$.conversion.from").value("USD"))
                                .andExpect(jsonPath("$.conversion.to").value("EUR"));

                mockMvc.perform(patch("/api/subscriptions/{id}/convert", id).param("to", "EUR"))
                                .andExpect(status().isNotFound());

                mockMvc.perform(post("/api/subscriptions/{id}/convert", id).param("to", "EUR"))
                                .andExpect(status().isMethodNotAllowed());
        }

        @Test
        void patchesSubscriptionOrReturnsNotFound() throws Exception {
                UUID id = UUID.randomUUID();
                when(subscriptionService.updateSubscription(eq(id), any(SubscriptionRequestDto.class)))
                                .thenReturn(response());
                String body = "{\"subscriptionName\":\"Streaming\"}";

                mockMvc.perform(patch("/api/subscriptions/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.subscriptionName").value("Streaming"));

                when(subscriptionService.updateSubscription(eq(id), any(SubscriptionRequestDto.class)))
                                .thenThrow(new SubscriptionNotFoundException(id));
                mockMvc.perform(patch("/api/subscriptions/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isNotFound());
        }

        @Test
        void rejectsCurrencyPatchWithoutReplacementPrice() throws Exception {
                UUID id = UUID.randomUUID();
                when(subscriptionService.updateSubscription(eq(id), any(SubscriptionRequestDto.class)))
                                .thenThrow(new IllegalArgumentException(
                                                "Provide a new subscriptionPrice or use the currency conversion endpoint"));

                mockMvc.perform(patch("/api/subscriptions/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"currency\":\"EUR\"}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void deletesSubscriptionOrReturnsNotFound() throws Exception {
                UUID id = UUID.randomUUID();

                mockMvc.perform(delete("/api/subscriptions/{id}", id))
                                .andExpect(status().isNoContent());
                verify(subscriptionService).deleteSubscription(id);

                doThrow(new RuntimeException("Subscription not found"))
                                .when(subscriptionService).deleteSubscription(id);
                mockMvc.perform(delete("/api/subscriptions/{id}", id))
                                .andExpect(status().isNotFound());
        }

        @Test
        void filtersSubscriptionsByDatesAndRanges() throws Exception {
                LocalDate subscriptionDate = LocalDate.of(2025, 1, 10);
                LocalDate renewalDate = LocalDate.of(2025, 2, 10);
                LocalDate startDate = LocalDate.of(2025, 2, 1);
                LocalDate endDate = LocalDate.of(2025, 2, 28);
                when(subscriptionService.getSubscriptionsBySubscriptionDate(subscriptionDate)).thenReturn(List.of());
                when(subscriptionService.getSubscriptionsByRenewalDate(renewalDate)).thenReturn(List.of());
                when(subscriptionService.getSubscriptionsByRenewalDateBetween(startDate, endDate))
                                .thenReturn(List.of());
                when(subscriptionService.getSubscriptionsBySubscriptionDateBetween(startDate, endDate))
                                .thenReturn(List.of());

                mockMvc.perform(get("/api/subscriptions/filter/subscription-date")
                                .param("date", subscriptionDate.toString()))
                                .andExpect(status().isOk());
                mockMvc.perform(get("/api/subscriptions/filter/renewal-date")
                                .param("date", renewalDate.toString()))
                                .andExpect(status().isOk());
                mockMvc.perform(get("/api/subscriptions/filter/renewal-range")
                                .param("startDate", startDate.toString())
                                .param("endDate", endDate.toString()))
                                .andExpect(status().isOk());
                mockMvc.perform(get("/api/subscriptions/filter/subscription-range")
                                .param("startDate", startDate.toString())
                                .param("endDate", endDate.toString()))
                                .andExpect(status().isOk());

                verify(subscriptionService).getSubscriptionsBySubscriptionDate(subscriptionDate);
                verify(subscriptionService).getSubscriptionsByRenewalDate(renewalDate);
                verify(subscriptionService).getSubscriptionsByRenewalDateBetween(startDate, endDate);
                verify(subscriptionService).getSubscriptionsBySubscriptionDateBetween(startDate, endDate);
        }

        private SubscriptionResponseDto response() {
                SubscriptionResponseDto response = new SubscriptionResponseDto();
                response.setSubscriptionId(UUID.randomUUID());
                response.setSubscriptionName("Streaming");
                response.setSubscriptionDate(LocalDate.of(2025, 1, 10));
                response.setRenewalDate(LocalDate.of(2025, 2, 10));
                response.setSubscriptionPrice(new BigDecimal("12.99"));
                response.setCurrency(Currency.USD);
                return response;
        }
}
