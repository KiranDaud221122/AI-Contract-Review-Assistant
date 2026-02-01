package com.contractreview.aiservice.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "ai_contract_reviews")
public class ReviewResult {

    @Id
    private String id;

    private String contractId;
    private String userId;
    private String originalFileName;
    private String summary;
    private String keyClauses;
    private String risksAndIssues;
    private String recommendations;
    private LocalDateTime reviewedAt;
    private String status;
    private String errorMessage;
}