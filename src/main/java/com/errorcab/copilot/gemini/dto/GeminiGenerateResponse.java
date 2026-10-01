package com.errorcab.copilot.gemini.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level response structure returned by Google Gemini generateContent endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiGenerateResponse {
    private List<GeminiCandidate> candidates = new ArrayList<>();

    public GeminiGenerateResponse() {}

    public List<GeminiCandidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<GeminiCandidate> candidates) {
        this.candidates = candidates != null ? candidates : new ArrayList<>();
    }

    public String getFirstCandidateText() {
        if (candidates != null) {
            for (GeminiCandidate candidate : candidates) {
                if (candidate != null) {
                    String text = candidate.extractFirstText();
                    if (text != null && !text.isBlank()) {
                        return text;
                    }
                }
            }
        }
        return null;
    }
}
