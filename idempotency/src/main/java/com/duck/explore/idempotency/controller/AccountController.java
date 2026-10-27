package com.duck.explore.idempotency.controller;

import com.duck.explore.idempotency.annotation.Idempotent;
import com.duck.explore.idempotency.dto.DepositRequest;
import com.duck.explore.idempotency.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Option 1: Uses SpEL to lock on accountNumber + referenceId for 120 seconds
    @PostMapping("/deposit")
    @Idempotent(key = "#depositRequest.accountNumber() + '-' + #depositRequest.referenceId()", ttl = 120, timeUnit = TimeUnit.SECONDS)
    public ResponseEntity<String> deposit(@RequestBody DepositRequest depositRequest) {
        accountService.processDeposit(depositRequest);
        return ResponseEntity.ok("Deposit processed");
    }

    // Option 2: Empty key automatically reads the 'Idempotency-Key' HTTP Header
    @PostMapping("/transfer")
    @Idempotent(ttl = 5, timeUnit = TimeUnit.MINUTES)
    public ResponseEntity<String> transfer(@RequestBody DepositRequest request) {
        accountService.processDeposit(request);
        return ResponseEntity.ok("Transfer processed");
    }
}