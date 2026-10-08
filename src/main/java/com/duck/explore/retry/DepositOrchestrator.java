package com.duck.explore.retry;

import com.duck.explore.client.ExternalPaymentGatewayClient;
import com.duck.explore.dto.DepositRequest;
import com.duck.explore.dto.DepositResponse;
import com.duck.explore.dto.GatewayDepositRequest;
import com.duck.explore.dto.GatewayDepositResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositOrchestrator {

    private final ExternalPaymentGatewayClient externalPaymentGatewayClient;


    public DepositResponse processDeposit(DepositRequest depositRequest, String idempotencyKey) {

        log.info("Processing deposit for account: {} with Idempotency Key: {}",
                depositRequest.getAccountNo(), idempotencyKey);

        GatewayDepositRequest gatewayDepositRequest = GatewayDepositRequest.builder()
                .accountNo(depositRequest.getAccountNo())
                .amount(depositRequest.getAmount())
                .currency(depositRequest.getCurrency())
                .build();

        GatewayDepositResponse gatewayResponse = externalPaymentGatewayClient.sendDeposit(gatewayDepositRequest, idempotencyKey);

        if (gatewayResponse == null) {
            return DepositResponse.builder()
                    .status("FAILED")
                    .processedAt(Instant.now())
                    .idempotencyKey(idempotencyKey)
                    .build();
        }

        return DepositResponse.builder()
                .transactionId(gatewayResponse.getTransactionId())
                .status("COMPLETED")
                .processedAt(gatewayResponse.getProcessedAt())
                .idempotencyKey(idempotencyKey)
                .build();
    }

}