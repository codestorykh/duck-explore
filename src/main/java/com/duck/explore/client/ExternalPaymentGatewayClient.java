package com.duck.explore.client;

import com.duck.explore.dto.GatewayDepositRequest;
import com.duck.explore.dto.GatewayDepositResponse;
import com.duck.explore.exception.RemoteServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalPaymentGatewayClient {

    private final RestClient paymentGatewayRestClient;
    private final RetryTemplate paymentGatewayRetryTemplate;

    /**
     * Retries up to 3 times on transient network drops or remote 5xx server errors.
     * Does NOT retry 4xx errors (client errors).
     */
    /*@Retryable(
            retryFor = {
                    // 1. Connection timeouts, read timeouts, connection reset, connection refused
                    ResourceAccessException.class,
                    // 2. Upstream server crashes, 502 Bad Gateway, 503 Service Unavailable, 504 Gateway Timeout
                    HttpServerErrorException.class
            },
            maxAttemptsExpression = "#{@externalGatewayProperties.retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "#{@externalGatewayProperties.retry.delay}",
                    multiplierExpression = "#{@externalGatewayProperties.retry.multiplier}",
                    randomExpression = "#{@externalGatewayProperties.retry.random}"
            )
    )*/
    public @Nullable GatewayDepositResponse sendDeposit(GatewayDepositRequest request, String idempotencyKey) {
        log.info("Sending deposit request to upstream gateway for reference: {}", idempotencyKey);

        // Execute the HTTP call inside the retry template's managed loop:
        return paymentGatewayRetryTemplate.execute(context ->
                paymentGatewayRestClient.post()
                        .uri("/api/v1/transactions/deposits")
                        .header("Idempotency-Key", idempotencyKey)
                        .body(request)
                        .retrieve()
                        .body(GatewayDepositResponse.class)
        );
    }

    /**
     * Fallback when all 3 retry attempts are exhausted.
     * The argument signature must match the original method (with the Exception as the first parameter).
     */
    @Recover
    public GatewayDepositResponse recover(Throwable ex, GatewayDepositRequest request, String idempotencyKey) {
        log.error("Exhausted all retries calling external gateway for reference: {}. Error: {}",
                idempotencyKey, ex.getMessage());

        throw new RemoteServiceUnavailableException("External payment partner unavailable. Please retry later.", ex);
    }

}