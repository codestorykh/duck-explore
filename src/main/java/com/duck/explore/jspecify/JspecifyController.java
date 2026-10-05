package com.duck.explore.jspecify;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class JspecifyController {

    private final TransactionPostingService transactionPostingService;

    public JspecifyController(TransactionPostingService transactionPostingService) {
        this.transactionPostingService = transactionPostingService;
    }

    @PostMapping("/jspecify")
    public ResponseEntity<String> jspecify(@RequestBody TransactionPostingRequest request) {
        transactionPostingService.process(request);

        return ResponseEntity.ok("jspecify");
    }
}
