package com.drjcodes.subadvise_api.exception;

public class CurrencyRateUnavailableException extends RuntimeException {

    public CurrencyRateUnavailableException(String message) {
        super(message);
    }

    public CurrencyRateUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}