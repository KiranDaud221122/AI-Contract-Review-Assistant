package com.contractreview.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private String contractId;
    private String userId;
    private String originalFileName;
    private String summary;
    private List<String> keyClauses;
    private List<String> risksAndIssues;
    private List<String> recommendations;
    private LocalDateTime reviewedAt;
    private String status;
    private String errorMessage;
}