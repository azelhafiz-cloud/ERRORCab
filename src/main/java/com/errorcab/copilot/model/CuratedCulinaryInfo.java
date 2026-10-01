package com.errorcab.copilot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 4-part structured culinary highlights categorized by signature food,
 * local cafe culture, traditional cuisine, and recommended experiences.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CuratedCulinaryInfo {
    private String signatureFood;
    private String localCafeCulture;
    private String traditionalCuisine;
    private List<String> recommendedExperiences = new ArrayList<>();

    public CuratedCulinaryInfo() {}

    public CuratedCulinaryInfo(String signatureFood, String localCafeCulture, String traditionalCuisine,
                               List<String> recommendedExperiences) {
        this.signatureFood = signatureFood;
        this.localCafeCulture = localCafeCulture;
        this.traditionalCuisine = traditionalCuisine;
        this.recommendedExperiences = recommendedExperiences != null ? recommendedExperiences : new ArrayList<>();
    }

    public String getSignatureFood() {
        return signatureFood;
    }

    public void setSignatureFood(String signatureFood) {
        this.signatureFood = signatureFood;
    }

    public String getLocalCafeCulture() {
        return localCafeCulture;
    }

    public void setLocalCafeCulture(String localCafeCulture) {
        this.localCafeCulture = localCafeCulture;
    }

    public String getTraditionalCuisine() {
        return traditionalCuisine;
    }

    public void setTraditionalCuisine(String traditionalCuisine) {
        this.traditionalCuisine = traditionalCuisine;
    }

    public List<String> getRecommendedExperiences() {
        return recommendedExperiences;
    }

    public void setRecommendedExperiences(List<String> recommendedExperiences) {
        this.recommendedExperiences = recommendedExperiences != null ? recommendedExperiences : new ArrayList<>();
    }
}
