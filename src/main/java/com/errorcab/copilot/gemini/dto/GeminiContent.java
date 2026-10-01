package com.errorcab.copilot.gemini.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a content block in a Gemini API request or response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiContent {
    private String role; // e.g. "user", "model", "system"
    private List<GeminiPart> parts = new ArrayList<>();

    public GeminiContent() {}

    public GeminiContent(String role, List<GeminiPart> parts) {
        this.role = role;
        this.parts = parts != null ? parts : new ArrayList<>();
    }

    public static GeminiContent userContent(String text) {
        GeminiContent content = new GeminiContent();
        content.setRole("user");
        content.getParts().add(new GeminiPart(text));
        return content;
    }

    public static GeminiContent systemContent(String text) {
        GeminiContent content = new GeminiContent();
        content.setRole("system");
        content.getParts().add(new GeminiPart(text));
        return content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<GeminiPart> getParts() {
        return parts;
    }

    public void setParts(List<GeminiPart> parts) {
        this.parts = parts != null ? parts : new ArrayList<>();
    }
}
