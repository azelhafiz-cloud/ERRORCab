package com.errorcab.copilot.gemini.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a candidate completion returned by the Gemini API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiCandidate {
    private GeminiContent content;
    private String finishReason;
    private Integer index;

    public GeminiCandidate() {}

    public GeminiContent getContent() {
        return content;
    }

    public void setContent(GeminiContent content) {
        this.content = content;
    }

    public String getFinishReason() {
        return finishReason;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    /**
     * Safely extracts the actual generated text/JSON payload from candidate content parts,
     * skipping intermediate thought/reasoning blocks (e.g. where thought = true).
     */
    public String extractFirstText() {
        if (content == null || content.getParts() == null || content.getParts().isEmpty()) {
            return null;
        }

        // 1. Look for the first non-thought part that contains non-blank text
        for (GeminiPart part : content.getParts()) {
            if (part != null && !Boolean.TRUE.equals(part.getThought())
                    && part.getText() != null && !part.getText().isBlank()) {
                return part.getText();
            }
        }

        // 2. If all parts are marked as thoughts, inspect if any part contains JSON structure
        for (GeminiPart part : content.getParts()) {
            if (part != null && part.getText() != null && !part.getText().isBlank()) {
                String trimmed = part.getText().trim();
                if (trimmed.startsWith("{") || trimmed.startsWith("```json") || trimmed.startsWith("```")) {
                    return part.getText();
                }
            }
        }

        // 3. Fallback: first non-blank text available across parts
        for (GeminiPart part : content.getParts()) {
            if (part != null && part.getText() != null && !part.getText().isBlank()) {
                return part.getText();
            }
        }

        return null;
    }
}
