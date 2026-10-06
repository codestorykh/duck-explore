package com.duck.explore.jakarta.bean.alidation;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final Validator validator;

    public void processInternalRequest(DepositRequest request) {

        // Proceed with validated business logic...
    }
}