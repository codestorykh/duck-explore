package com.duck.explore.validation;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrencyValidatorTest {

    private CurrencyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CurrencyValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {"USD", "KHR", "EUR", "SGD", "JPY", "GBP", "usd"})
    void shouldAcceptValidCurrencies(@Nullable String currency) {
        assertTrue(validator.isValid(currency, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"XYZ", "123", "US", "USDD", "EURO"})
    void shouldRejectInvalidCurrencies(@Nullable String currency) {
        assertFalse(validator.isValid(currency, null));
    }

    @Test
    void shouldPassOnNullOrBlank() {
        // Must delegate null check to @NotNull / @NotBlank
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("   ", null));
    }
}