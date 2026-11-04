package com.drjcodes.subadvise_api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.drjcodes.subadvise_api.dto.CurrencyConversionResponseDto;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.service.CurrencyConversionService;
import com.drjcodes.subadvise_api.exception.CurrencyRateUnavailableException;
import com.drjcodes.subadvise_api.exception.UnsupportedCurrencyException;

class CurrencyControllerTest {

        private CurrencyConversionService conversionService;
        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
                conversionService = org.mockito.Mockito.mock(CurrencyConversionService.class);
                mockMvc = MockMvcBuilders.standaloneSetup(new CurrencyController(conversionService))
                                .setControllerAdvice(new ApiExceptionHandler())
                                .build();
        }

        @Test
        void convertsAmountAndReturnsRateMetadata() throws Exception {
                CurrencyConversionResponseDto response = new CurrencyConversionResponseDto();
                response.setAmount(new BigDecimal("10.00"));
                response.setFrom(Currency.USD);
                response.setTo(Currency.EUR);
                response.setConvertedAmount(new BigDecimal("9.20"));
                response.setRateLastUpdated(Instant.parse("2026-10-17T00:00:00Z"));
                response.setRateNextUpdate(Instant.parse("2026-10-18T00:00:00Z"));
                response.setRateProvider("ExchangeRate-API Open Access");
                response.setAttributionUrl("https://www.exchangerate-api.com");
                when(conversionService.convert(new BigDecimal("10.00"), Currency.USD, Currency.EUR))
                                .thenReturn(response);

                mockMvc.perform(get("/api/currency/convert")
                                .param("amount", "10.00")
                                .param("from", "USD")
                                .param("to", "EUR"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.convertedAmount").value(9.20))
                                .andExpect(jsonPath("$.from").value("USD"))
                                .andExpect(jsonPath("$.to").value("EUR"))
                                .andExpect(jsonPath("$.rateProvider").value("ExchangeRate-API Open Access"))
                                .andExpect(jsonPath("$.attributionUrl").value("https://www.exchangerate-api.com"));
        }

        @Test
        void returnsUnprocessableEntityWhenCurrencyHasNoRate() throws Exception {
                when(conversionService.convert(new BigDecimal("1"), Currency.USD, Currency.BOV))
                                .thenThrow(new UnsupportedCurrencyException(
                                                "No current exchange rate is available for currency BOV"));

                mockMvc.perform(get("/api/currency/convert")
                                .param("amount", "1")
                                .param("from", "USD")
                                .param("to", "BOV"))
                                .andExpect(status().is(422))
                                .andExpect(jsonPath("$.error")
                                                .value("No current exchange rate is available for currency BOV"));
        }

        @Test
        void returnsServiceUnavailableWhenRatesCannotBeRetrieved() throws Exception {
                when(conversionService.convert(new BigDecimal("1"), Currency.USD, Currency.EUR))
                                .thenThrow(new CurrencyRateUnavailableException(
                                                "Could not retrieve current exchange rates"));

                mockMvc.perform(get("/api/currency/convert")
                                .param("amount", "1")
                                .param("from", "USD")
                                .param("to", "EUR"))
                                .andExpect(status().isServiceUnavailable());
        }
}
