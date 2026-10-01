package com.errorcab.copilot.gemini.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level request body for Google Gemini API generateContent endpoint.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiGenerateRequest {
    private List<GeminiContent> contents = new ArrayList<>();
    private GeminiContent systemInstruction;
    private GeminiGenerationConfig generationConfig;

    public GeminiGenerateRequest() {}

    public GeminiGenerateRequest(List<GeminiContent> contents, GeminiGenerationConfig generationConfig) {
        this.contents = contents != null ? contents : new ArrayList<>();
        this.generationConfig = generationConfig;
    }

    public List<GeminiContent> getContents() {
        return contents;
    }

    public void setContents(List<GeminiContent> contents) {
        this.contents = contents != null ? contents : new ArrayList<>();
    }

    public GeminiContent getSystemInstruction() {
        return systemInstruction;
    }

    public void setSystemInstruction(GeminiContent systemInstruction) {
        this.systemInstruction = systemInstruction;
    }

    public GeminiGenerationConfig getGenerationConfig() {
        return generationConfig;
    }

    public void setGenerationConfig(GeminiGenerationConfig generationConfig) {
        this.generationConfig = generationConfig;
    }
}
