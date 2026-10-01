package com.errorcab.copilot.destination.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Factual safety advisory or place notice with verified source attribution.
 * Scams, warnings, and alerts must always carry an authoritative source.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SafetyAdvisory {
    private String text;
    private String source;
    private String category; // "WEATHER", "TRANSPORT", "TERRAIN", "GENERAL"

    public SafetyAdvisory() {}

    public SafetyAdvisory(String text, String source, String category) {
        this.text = text;
        this.source = source != null && !source.isBlank() ? source : "No verified source available";
        this.category = category != null ? category : "GENERAL";
    }

    public static SafetyAdvisory unverified(String text) {
        return new SafetyAdvisory(text, "No verified advisory available", "GENERAL");
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
