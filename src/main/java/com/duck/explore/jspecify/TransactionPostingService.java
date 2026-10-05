package com.duck.explore.jspecify;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TransactionPostingService {

    // Parameters and return are non-null by default
    @Nullable
    public TransactionRecord process(TransactionPostingRequest request) {
        String ref = resolveReference(request.externalRefCode());
        if(Objects.isNull(ref)) {
            return null;
        }
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