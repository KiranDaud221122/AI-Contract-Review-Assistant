package com.contractreview.contractservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/contracts")
public class ContractTestController {

    @GetMapping("/test")
    public String test() {
        return "Hello from Contract Service! Time: " + LocalDateTime.now();
    }
}