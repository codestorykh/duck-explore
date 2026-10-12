package com.duck.explore.service;

import com.duck.explore.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public String hello(String name) {
        var account = accountRepository.findByAccountNumber("12345678");
        if(StringUtils.hasText(name) && account.isPresent()) {
            return "Hello " + name;
        }

        return "Hello";
    }
}
