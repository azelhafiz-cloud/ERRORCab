package com.errorcab.copilot.gemini.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Individual content part in Gemini payload.
 * Supports standard text parts as well as reasoning/thought parts with thoughtSignature.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiPart {
    private String text;
    private String thoughtSignature;
    private Boolean thought;

    public GeminiPart() {}

    public GeminiPart(String text) {
        this.text = text;
    }

    public GeminiPart(String text, String thoughtSignature, Boolean thought) {
        this.text = text;
        this.thoughtSignature = thoughtSignature;
        this.thought = thought;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getThoughtSignature() {
        return thoughtSignature;
    }

    public void setThoughtSignature(String thoughtSignature) {
        this.thoughtSignature = thoughtSignature;
    }

    public Boolean getThought() {
        return thought;
    }

    public void setThought(Boolean thought) {
        this.thought = thought;
    }
}
