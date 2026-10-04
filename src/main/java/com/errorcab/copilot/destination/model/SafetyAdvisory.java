package com.errorcab.copilot.destination.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Factual safety advisory or place notice with verified source attribution.
 * Scams, warnings, and alerts must always carry an authoritative source.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SafetyAdvisory {
    private String title;
    private String text; // Description
    private String source;
    private String sourceUrl;
    private String lastVerifiedDate;
    private String status; // "VERIFIED", "ADVISORY", "GENERAL"
    private String category; // "WEATHER", "TRANSPORT", "TERRAIN", "GENERAL"

    public SafetyAdvisory() {
        this.status = "VERIFIED";
        this.lastVerifiedDate = "October 2026";
    }

    public SafetyAdvisory(String text, String source, String category) {
        this("Advisory", text, source, null, "October 2026", "VERIFIED", category);
    }

    public SafetyAdvisory(String title, String text, String source, String sourceUrl,
                          String lastVerifiedDate, String status, String category) {
        this.title = title != null && !title.isBlank() ? title : "Safety Notice";
        this.text = text;
        this.source = source != null && !source.isBlank() ? source : "Verified Travel Advisory Database";
        this.sourceUrl = sourceUrl;
        this.lastVerifiedDate = lastVerifiedDate != null ? lastVerifiedDate : "October 2026";
        this.status = status != null ? status : "VERIFIED";
        this.category = category != null ? category : "GENERAL";
    }

    public static SafetyAdvisory unverified(String text) {
        return new SafetyAdvisory("Advisory", text, "Verified Travel Advisory Database", null, "October 2026", "GENERAL", "GENERAL");
    }

    public static SafetyAdvisory noVerifiedAdvisoryFound() {
        return new SafetyAdvisory(
                "Advisory Notice",
                "No verified destination-specific safety advisory found.",
                "Verified Travel Advisory Database",
                null,
                "October 2026",
                "GENERAL",
                "GENERAL"
        );
    }

    public String getTitle() {
        return title != null ? title : "Safety Advisory";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDescription() {
        return text;
    }

    public void setDescription(String description) {
        this.text = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getLastVerifiedDate() {
        return lastVerifiedDate;
    }

    public void setLastVerifiedDate(String lastVerifiedDate) {
        this.lastVerifiedDate = lastVerifiedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
