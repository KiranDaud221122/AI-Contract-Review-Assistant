package com.contractreview.aiservice.service;

import com.contractreview.aiservice.dto.GeminiRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public String getAnswer(String prompt) {
        log.info("Calling Gemini API with prompt (length: {} chars)", prompt.length());

        GeminiRequest request = GeminiRequest.fromPrompt(prompt);

        return webClient.post()
                .uri(geminiApiUrl + "?key=" + geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.isError(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Gemini error: " + body)))
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .doOnError(e -> log.error("Gemini API call failed", e))
                .block();
    }
}