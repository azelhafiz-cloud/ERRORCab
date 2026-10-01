package com.errorcab.copilot.gemini.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Generation configuration for Gemini API, enforcing JSON output.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiGenerationConfig {
    private String responseMimeType; // "application/json"
    private Double temperature;
    private Integer maxOutputTokens;
    private Double topP;
    private Integer topK;

    public GeminiGenerationConfig() {}

    public GeminiGenerationConfig(String responseMimeType, Double temperature) {
        this.responseMimeType = responseMimeType;
        this.temperature = temperature;
    }

    public static GeminiGenerationConfig jsonConfig(double temperature) {
        GeminiGenerationConfig config = new GeminiGenerationConfig();
        config.setResponseMimeType("application/json");
        config.setTemperature(temperature);
        return config;
    }

    public String getResponseMimeType() {
        return responseMimeType;
    }

    public void setResponseMimeType(String responseMimeType) {
        this.responseMimeType = responseMimeType;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxOutputTokens() {
        return maxOutputTokens;
    }

    public void setMaxOutputTokens(Integer maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
    }

    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }
}
