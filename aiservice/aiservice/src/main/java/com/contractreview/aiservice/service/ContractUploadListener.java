package com.contractreview.aiservice.service;

import com.contractreview.aiservice.entity.ReviewResult;
import com.contractreview.aiservice.event.ContractUploadedEvent;
import com.contractreview.aiservice.repository.ReviewResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractUploadListener {

    private final ContractReviewAIService reviewService;
    private final ReviewResultRepository reviewRepository;

    @KafkaListener(topics = "${kafka.topic.name:contract-uploaded}", groupId = "ai-review-group")
    public void processContract(ContractUploadedEvent event) {
        log.info("Received contract event | contractId: {}", event.getContractId());
        ReviewResult result = reviewService.generateReview(event);
        reviewRepository.save(result);
        log.info("AI review saved for contractId: {}", event.getContractId());
    }
}