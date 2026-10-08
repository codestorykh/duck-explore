package com.duck.explore.retry;

import com.duck.explore.dto.DepositRequest;
import com.duck.explore.entity.Account;
import com.duck.explore.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountTransactionalWorker {

    private final AccountRepository accountRepository;

    /**
     * REQUIRES_NEW guarantees each retry attempts work inside an isolated, fresh transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeDeposit(DepositRequest request) {
        log.debug("Attempting pessimistic write lock for account: {}", request.getAccountNo());

        Account account = accountRepository.findByAccountNumber(request.getAccountNo())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.getAccountNo()));

        account.credit(request.getAmount());
        accountRepository.save(account);
    }
}