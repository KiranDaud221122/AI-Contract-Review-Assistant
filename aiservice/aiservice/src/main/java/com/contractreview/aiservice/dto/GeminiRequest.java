package com.contractreview.aiservice.dto;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public record GeminiRequest(List<Content> contents) {

    public static GeminiRequest fromPrompt(String prompt) {
        log.info("Creating GeminiRequest from prompt (length: {} chars)", prompt.length());
        return new GeminiRequest(
                List.of(new Content(
                        List.of(new Part(prompt))
                ))
        );
    }

    public record Content(List<Part> parts) {}
    public record Part(String text) {}
}