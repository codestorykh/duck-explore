package com.duck.explore.archunit;

import com.duck.explore.dto.DepositResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArchUnitController {

    @Autowired
    ArchUnitService archUnitService;

    @GetMapping("/name")
    public ResponseEntity<String> hello(@RequestParam String name) {
        System.out.println("Intercept hello " + name);
        return ResponseEntity.ok(archUnitService.hello(name));
    }
}
