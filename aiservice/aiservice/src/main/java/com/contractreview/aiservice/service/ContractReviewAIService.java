package com.contractreview.aiservice.service;

import com.contractreview.aiservice.dto.GeminiRequest;
import com.contractreview.aiservice.entity.ReviewResult;
import com.contractreview.aiservice.event.ContractUploadedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractReviewAIService {

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;  // ← Inject this (add @Bean in config if not already)

    public ReviewResult generateReview(ContractUploadedEvent event) {
        log.info("Generating AI review for contractId: {}", event.getContractId());

        String prompt = createReviewPrompt(event);
        String aiResponse = geminiService.getAnswer(prompt);

        ReviewResult result = processAiResponse(event, aiResponse);
        result.setStatus("COMPLETED");
        result.setReviewedAt(LocalDateTime.now());

        log.info("Generated review for contractId: {}", event.getContractId());

        return result;
    }

    private String createReviewPrompt(ContractUploadedEvent event) {
        return String.format("""
            You are an expert contract lawyer.
            Analyze the following contract details and return **ONLY** valid JSON (no markdown, no extra text):
            {
              "summary": "One paragraph summary",
              "keyClauses": ["Clause 1", "Clause 2", ...],
              "risksAndIssues": ["Risk 1", "Risk 2", ...],
              "recommendations": ["Recommendation 1", "Recommendation 2", ...]
            }

            Contract details:
            File: %s
            Size: %d bytes
            User: %s

            Full contract text: [extracted text would go here]

            Respond **ONLY** with the JSON object.
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
        r.setReviewedAt(LocalDateTime.now());
        r.setStatus("COMPLETED");

        try {
            // Step 1: Parse Gemini full response
            JsonNode root = objectMapper.readTree(aiResponse);

            // Step 2: Extract the actual text from candidates
            JsonNode textNode = root.path("candidates")
                    .get(0)  // first candidate
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");


            if (textNode.isMissingNode()) {
                throw new RuntimeException("No text found in Gemini response");
            }

            String jsonText = textNode.asText().trim();

            jsonText = jsonText.replaceAll("(?s)^```json\\s*(.*?)\\s*```$", "$1").trim();
            JsonNode analysis = objectMapper.readTree(jsonText);

            // Step 5: Set fields with safe defaults
            r.setSummary(analysis.path("summary").asText("No summary available"));
            r.setKeyClauses(analysis.path("keyClauses").toString());
            r.setRisksAndIssues(analysis.path("risksAndIssues").toString());
            r.setRecommendations(analysis.path("recommendations").toString());

            // Step 6: Print beautiful formatted report
            printBeautifulReviewReport(r, analysis);

        } catch (Exception e) {
            log.error("Failed to parse Gemini response for contractId: {}", event.getContractId(), e);
            r.setSummary(aiResponse); // fallback: raw response
            r.setErrorMessage("Parsing failed: " + e.getMessage());
            // Optional: print error report
            System.out.println("AI Review Parsing Failed: " + e.getMessage());
        }

        return r;
    }

    private void printBeautifulReviewReport(ReviewResult r, JsonNode analysis) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("          Contract Review Report (Easy to Read)          ");
        System.out.println("=".repeat(60));

        System.out.printf("Contract ID     : %s%n", r.getContractId());
        System.out.printf("User            : %s%n", r.getUserId());
        System.out.printf("File Name       : %s%n", r.getOriginalFileName());
        System.out.printf("Review Status   : %s%n", r.getStatus());
        System.out.printf("Reviewed At     : %s%n%n", r.getReviewedAt());

        // 1. Summary
        System.out.println("1. Summary (One-paragraph overview)");
        System.out.println(analysis.path("summary").asText("No summary available"));
        System.out.println();

        // 2. Key Clauses
        System.out.println("2. Key Clauses (Main points of the contract)");
        JsonNode clauses = analysis.path("keyClauses");
        if (clauses.isArray() && !clauses.isEmpty()) {
            clauses.forEach(c -> System.out.println("• " + c.asText()));
        } else {
            System.out.println("• No key clauses found");
        }
        System.out.println();

        // 3. Risks & Issues
        System.out.println("3. Risks & Issues (Potential red flags)");
        JsonNode risks = analysis.path("risksAndIssues");
        if (risks.isArray() && !risks.isEmpty()) {
            risks.forEach(risk -> System.out.println("• " + risk.asText()));
        } else {
            System.out.println("• No risks identified");
        }
        System.out.println();

        // 4. Recommendations
        System.out.println("4. Recommendations (What to negotiate / fix)");
        JsonNode recs = analysis.path("recommendations");
        if (recs.isArray() && !recs.isEmpty()) {
            recs.forEach(rec -> System.out.println("• " + rec.asText()));
        } else {
            System.out.println("• No recommendations available");
        }
        System.out.println();

        System.out.println("Saved Successfully → Review stored in database for contract ID: " + r.getContractId());
        System.out.println("=".repeat(60) + "\n");
    }
}