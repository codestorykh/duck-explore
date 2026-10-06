package com.duck.explore.validation.error;

import jakarta.validation.Payload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public final class ValidationErrorCode {

    @Getter
    @RequiredArgsConstructor
    public enum Code {
        VAL_ACCOUNT_BLANK("ERR_VAL_001", "Account number must not be blank"),
        VAL_ACCOUNT_FORMAT("ERR_VAL_002", "Account number format is invalid"),
        VAL_AMOUNT_NULL("ERR_VAL_003", "Amount is required"),
        VAL_AMOUNT_MIN("ERR_VAL_004", "Amount must meet the minimum threshold"),
        VAL_CURRENCY_INVALID("ERR_VAL_005", "Currency code is invalid"),
        VAL_GENERIC_INVALID("ERR_VAL_999", "Field value failed validation");

        private final String code;
        private final String defaultMessage;
    }

    // Concrete payload marker interfaces
    public interface AccountBlank extends Payload {}
    public interface AccountFormat extends Payload {}
    public interface AmountNull extends Payload {}
    public interface AmountMin extends Payload {}
    public interface CurrencyInvalid extends Payload {}
}