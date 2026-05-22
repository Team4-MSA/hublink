package com.msa.ai_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AiRequest {
    private List<Content> contents;

    public static AiRequest of(String prompt) {
        return new AiRequest(
                List.of(
                        new Content(
                                List.of(new Part(prompt))
                        )
                )
        );
    }

    @Getter
    @AllArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    @Getter
    @AllArgsConstructor
    public static class Part {
        private String text;
    }
}

