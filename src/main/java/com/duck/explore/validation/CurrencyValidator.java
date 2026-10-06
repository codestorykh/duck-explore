package com.duck.explore.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jspecify.annotations.Nullable;

import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    // Pre-load all ISO-4217 currency codes supported by the JVM
    private static final Set<String> VALID_CURRENCIES = Currency.getAvailableCurrencies()
            .stream()
            .map(Currency::getCurrencyCode)
            .collect(Collectors.toUnmodifiableSet());

    @Override
    public boolean isValid(@Nullable String value, @Nullable ConstraintValidatorContext context) {
        // Jakarta convention: let @NotNull or @NotBlank handle null/empty checks
        if (value == null || value.isBlank()) {
            return true;
        }

        return VALID_CURRENCIES.contains(value.trim().toUpperCase());
    }
}