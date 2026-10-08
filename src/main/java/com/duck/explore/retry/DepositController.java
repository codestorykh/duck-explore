package com.duck.explore.retry;

import com.duck.explore.dto.DepositRequest;
import com.duck.explore.dto.DepositResponse;
import com.duck.explore.utils.IdempotencyKeyGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final DepositOrchestrator depositOrchestrator;
    private final IdempotencyKeyGenerator idempotencyKeyGenerator;

    @PostMapping
    public ResponseEntity<DepositResponse> processDeposit(@Valid @RequestBody DepositRequest request,
                                                          @RequestHeader(value = "Idempotency-Key", required = false) String clientKey) {

        // Use client-provided key or generate a unique transaction token
        String resolvedKey = (clientKey != null && !clientKey.isBlank())
                ? clientKey
                : idempotencyKeyGenerator.generateRandomKey("dep");

        DepositResponse response = depositOrchestrator.processDeposit(request, resolvedKey);
        return ResponseEntity.ok(response);
    }

}