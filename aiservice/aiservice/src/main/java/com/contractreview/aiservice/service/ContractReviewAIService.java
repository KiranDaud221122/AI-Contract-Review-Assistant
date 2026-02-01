package com.contractreview.aiservice.service;

import com.contractreview.aiservice.entity.ReviewResult;
import com.contractreview.aiservice.event.ContractUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractReviewAIService {

    private final GeminiService geminiService;

    public ReviewResult generateReview(ContractUploadedEvent event) {
        log.info("Generating AI review for contractId: {}", event.getContractId());

        String prompt = createReviewPrompt(event);

        String aiResponse = geminiService.getAnswer(prompt);

        ReviewResult result = processAiResponse(event, aiResponse);
        result.setStatus("COMPLETED");
        result.setReviewedAt(LocalDateTime.now());
        System.out.println(result);

        return result;
    }

    private String createReviewPrompt(ContractUploadedEvent event) {
        return String.format("""
            You are an expert contract lawyer.
            Analyze the following contract details and return a JSON object with:
            - summary: one paragraph summary
            - keyClauses: bullet list of main clauses
            - risksAndIssues: list of potential risks/red flags
            - recommendations: list of negotiation/safety suggestions 

            Contract details:
            File: %s
            Size: %d bytes
            User: %s

            Full contract text: [extracted text would go here]

            Respond ONLY with valid JSON.
            """,
                event.getOriginalFileName(),
                event.getFileSizeBytes(),
                event.getUserId());
    }

    private ReviewResult processAiResponse(ContractUploadedEvent event, String aiResponse) {
        ReviewResult r = new ReviewResult();
        r.setContractId(event.getContractId());
        r.setUserId(event.getUserId());
        r.setOriginalFileName(event.getOriginalFileName());
        r.setSummary(aiResponse);
        return r;
    }
}