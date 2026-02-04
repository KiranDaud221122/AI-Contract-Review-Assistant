package com.contractreview.aiservice.controller;

import com.contractreview.aiservice.dto.ReviewResponse;
import com.contractreview.aiservice.entity.ReviewResult;
import com.contractreview.aiservice.repository.ReviewResultRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewResultRepository reviewRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResult>> getUserReviews(@PathVariable String userId) {
        List<ReviewResult> reviews = reviewRepository.findByUserId(userId);
        return ResponseEntity.ok(reviews);
    }



    @GetMapping("/contract/{contractId}")
    public ResponseEntity<ReviewResponse> getContractReview(@PathVariable String contractId) {
        return reviewRepository.findByContractId(contractId)
                .map(contract -> {
                    ReviewResponse response = ReviewResponse.builder()
                            .contractId(contract.getContractId())
                            .userId(contract.getUserId())
                            .originalFileName(contract.getOriginalFileName())
                            .summary(contract.getSummary())
                            .keyClauses(parseJsonList(contract.getKeyClauses()))
                            .risksAndIssues(parseJsonList(contract.getRisksAndIssues()))
                            .recommendations(parseJsonList(contract.getRecommendations()))
                            .reviewedAt(contract.getReviewedAt())
                            .status(contract.getStatus())
                            .errorMessage(contract.getErrorMessage())
                            .build();
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Helper to parse JSON string to List<String>
    private List<String> parseJsonList(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON list: {}", json);
            return List.of(json); // fallback
        }
    }

}