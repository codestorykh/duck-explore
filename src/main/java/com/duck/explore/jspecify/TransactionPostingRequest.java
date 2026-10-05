package com.duck.explore.jspecify;

import java.math.BigDecimal;

import org.jspecify.annotations.Nullable;

public record TransactionPostingRequest(
        String transactionId,            // Non-null by default
        BigDecimal amount,               // Non-null by default
        String currency,                 // Non-null by default
        @Nullable String narration,      // Optional/Nullable
        @Nullable String externalRefCode // Optional/Nullable
) {
}