package com.duck.explore.idempotency.dto;

import java.math.BigDecimal;

public record DepositRequest(
        String accountNumber,
        BigDecimal amount,
        String currency,
        String reference
) {
}
