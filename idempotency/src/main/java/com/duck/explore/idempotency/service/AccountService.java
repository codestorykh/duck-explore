package com.duck.explore.idempotency.service;

import com.duck.explore.idempotency.dto.DepositRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountService {

    public String processDeposit(DepositRequest depositRequest) {
        return "Successfully.";
    }
}
