package com.drjcodes.subadvise_api.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.drjcodes.subadvise_api.exception.CurrencyRateUnavailableException;
import com.drjcodes.subadvise_api.exception.SubscriptionNotFoundException;
import com.drjcodes.subadvise_api.exception.TargetNotFoundException;
import com.drjcodes.subadvise_api.exception.UnsupportedCurrencyException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSubscriptionNotFound(SubscriptionNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(TargetNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTargetNotFound(TargetNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(UnsupportedCurrencyException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedCurrency(UnsupportedCurrencyException exception) {
        return ResponseEntity.status(422).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(CurrencyRateUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleRateUnavailable(CurrencyRateUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", exception.getMessage()));
    }
}