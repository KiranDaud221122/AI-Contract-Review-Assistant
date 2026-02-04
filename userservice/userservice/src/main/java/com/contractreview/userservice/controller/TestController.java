package com.contractreview.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final WebClient contractClient; // jo tumne @Bean banaya hai

    @GetMapping("/call-contract")
    public Mono<String> testContractCall() {
        return contractClient
                .get()
                .uri("lb://contract-service/api/contracts/test")  // Contract Service ka ek endpoint
                .retrieve()
                .bodyToMono(String.class)
                .onErrorReturn("Contract Service not reachable");
    }
}