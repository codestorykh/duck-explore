package com.duck.explore.exception;

public class AccountConcurrencyException extends RuntimeException {
    public AccountConcurrencyException(String message) {
        super(message);
    }
}