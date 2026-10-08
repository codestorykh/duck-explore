package com.duck.explore.exception;

public class GatewayReadTimeoutException extends RuntimeException {
    public GatewayReadTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}