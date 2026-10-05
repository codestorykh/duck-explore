package com.duck.explore.jspecify;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
public class TransactionPostingService {

    // Parameters and return are non-null by default
    //@Nullable  // If I comment this annotation, when commit the pre-commit githooks will not allow to commit.
    public TransactionRecord process(TransactionPostingRequest request) {
        String ref = resolveReference(request.externalRefCode());
        if (!StringUtils.hasText(ref)) {
            ref = "DEFAULT-REF";
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