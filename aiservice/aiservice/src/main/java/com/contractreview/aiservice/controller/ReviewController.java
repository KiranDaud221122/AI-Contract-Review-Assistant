package com.contractreview.aiservice.controller;

import com.contractreview.aiservice.entity.ReviewResult;
import com.contractreview.aiservice.repository.ReviewResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewResultRepository reviewRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResult>> getUserReviews(@PathVariable String userId) {
        List<ReviewResult> reviews = reviewRepository.findByUserId(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<ReviewResult> getContractReview(@PathVariable String contractId) {
        return reviewRepository.findByContractId(contractId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }




}