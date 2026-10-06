package com.duck.explore.jspecify;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;

@NullMarked
@Service
public class RestClient {

    private final org.springframework.web.client.RestClient restClient = org.springframework.web.client.RestClient.create();

    public record BalanceResponse(String accountNo, long balanceCents) {
    }

    public long fetchBalance(String accountNo) {
        BalanceResponse response = restClient.get()
                .uri("/accounts/{id}/balance", accountNo)
                .retrieve()
                .body(BalanceResponse.class);

        // we need to check it to make noncomplie time error
        if (response == null) {
            throw new IllegalStateException("Empty balance response payload received from core banking");
        }

        // it compiles time error, so we need to check null
        return response.balanceCents(); // Safe: guarded by preceding check
    }
}