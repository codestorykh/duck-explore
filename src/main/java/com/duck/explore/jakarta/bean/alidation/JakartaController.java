package com.duck.explore.jakarta.bean.alidation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("jakarta")
@RequiredArgsConstructor
public class JakartaController {

    private final DepositService depositService;

    @PostMapping("/deposit")
    public ResponseEntity<String> processDeposit(@Valid @RequestBody DepositRequest request) {
        depositService.processInternalRequest(request);
        return ResponseEntity.ok("Successfully");
    }

}
