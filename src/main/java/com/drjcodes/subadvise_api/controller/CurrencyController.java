package com.drjcodes.subadvise_api.controller;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.drjcodes.subadvise_api.dto.CurrencyConversionResponseDto;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.service.CurrencyConversionService;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    private final CurrencyConversionService conversionService;

    public CurrencyController(CurrencyConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @GetMapping("/convert")
    public CurrencyConversionResponseDto convert(
            @RequestParam BigDecimal amount,
            @RequestParam Currency from,
            @RequestParam Currency to) {
        return conversionService.convert(amount, from, to);
    }

}