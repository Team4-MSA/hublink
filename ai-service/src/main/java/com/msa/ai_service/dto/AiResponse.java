package com.msa.ai_service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AiResponse {
    private List<Candidate> candidates;

    @Getter
    @NoArgsConstructor
    public static class Candidate {
        private Content content;
    }

    @Getter
    @NoArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    @Getter
    @NoArgsConstructor
    public static class Part {
        private String text;
    }

    public String getText() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        Content content = candidates.get(0).getContent();
        if (content == null || content.getParts() == null || content.getParts().isEmpty()) {
            return null;
        }

        return content.getParts().get(0).getText();
    }
}
