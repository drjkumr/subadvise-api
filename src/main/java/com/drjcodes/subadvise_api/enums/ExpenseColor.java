package com.drjcodes.subadvise_api.enums;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ExpenseColor {
    RED, ORANGE, YELLOW, LIME, GREEN;

    @JsonValue
    public String toJsonValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}