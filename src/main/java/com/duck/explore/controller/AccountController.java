package com.duck.explore.controller;

import com.duck.explore.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {


    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/name")
    public ResponseEntity<String> hello(@RequestParam String name) {
        System.out.println("Intercept request hello " + name);
        try {
            return ResponseEntity.ok(accountService.hello(name));
        }catch (Exception ex){
           ex.printStackTrace();
           return ResponseEntity.status(500).build();
        }
    }

}
