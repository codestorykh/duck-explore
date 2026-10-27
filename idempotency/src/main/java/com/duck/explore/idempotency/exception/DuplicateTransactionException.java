package com.duck.explore.idempotency.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateTransactionException extends RuntimeException {

    private final String idempotencyKey;

    public DuplicateTransactionException(String idempotencyKey) {
        super("Duplicate or concurrent transaction rejected for key: " + idempotencyKey);
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}