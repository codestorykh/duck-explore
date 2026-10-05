package com.duck.explore.jspecify;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class TransactionPostingService {

    // Parameters and return are non-null by default
    public TransactionRecord process(TransactionPostingRequest request) {
        String ref = resolveReference(request.externalRefCode());
        return new TransactionRecord(request.transactionId(), ref);
    }

    // Explicitly declaring nullable parameter and return
    public @Nullable String resolveReference(@Nullable String externalRef) {
        if (externalRef == null || externalRef.isBlank()) {
            return null;
        }
        return "EXT-" + externalRef.trim();
    }
}