package com.errorcab.copilot.destination.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Normalized destination intelligence profile representing authentic,
 * verified geographical, cultural, culinary, and safety data.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DestinationProfile {
    private String destinationName;
    private String district;
    private String region;
    private String state;
    private String country = "India";
    private double latitude;
    private double longitude;
    private String destinationType;
    private String shortDescription;
    private List<String> majorHighlights = new ArrayList<>();
    private List<String> attractions = new ArrayList<>();
    private List<String> heritageHighlights = new ArrayList<>();
    private List<String> natureHighlights = new ArrayList<>();
    private List<String> photographySpots = new ArrayList<>();
    private List<String> shoppingHighlights = new ArrayList<>();
    private List<String> culinaryHighlights = new ArrayList<>();
    private List<String> localSpecialities = new ArrayList<>();
    private List<String> suggestedActivities = new ArrayList<>();
    private String typicalTripDuration;
    private List<String> localTravelAdvice = new ArrayList<>();
    private String transportAdvice;
    private String familySuitability;
    private String budgetNotes;
    private String bestKnownFor;
    private List<SafetyAdvisory> safetyNotes = new ArrayList<>();
    private String sourceInformation;
    private String knowledgeStatus; // "VERIFIED_LOCAL_KNOWLEDGE", "DISCOVERED_PUBLIC_DATA", "LIMITED_FRAMEWORK"

    public DestinationProfile() {
        this.knowledgeStatus = "VERIFIED_LOCAL_KNOWLEDGE";
        this.sourceInformation = "ERRORCab Verified Kerala Travel Knowledge Base";
    }

    public static DestinationProfile createLimited(String name) {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName(name);
        p.setRegion("Regional");
        p.setState("India");
        p.setDestinationType("General Destination");
        p.setShortDescription("Destination information is limited. ERRORCab has generated a planning framework without inventing destination-specific facts.");
        p.setKnowledgeStatus("LIMITED_FRAMEWORK");
        p.setSourceInformation("ERRORCab General Planning Framework (Zero Hallucination Guarantee)");
        p.getSafetyNotes().add(new SafetyAdvisory(
                "No verified destination-specific advisory is currently available.",
                "Verified Travel Advisory Database",
                "GENERAL"
        ));
        p.setTypicalTripDuration("Half-day / Flexible");
        p.setTransportAdvice("Pre-book direct ERRORCab transit for guaranteed upfront pricing.");
        p.setFamilySuitability("Suitable for all travelers.");
        p.setBudgetNotes("Standard regional travel budget.");
        p.setBestKnownFor("Local regional exploration");
        return p;
    }

    public static DestinationProfile createUnresolved(String name) {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName(name != null ? name : "Unknown Destination");
        p.setRegion("Unknown");
        p.setState("India");
        p.setDestinationType("Unresolved Location");
        p.setShortDescription("Couldn't confidently locate this destination. Try adding the district or state.");
        p.setKnowledgeStatus("UNRESOLVED");
        p.setSourceInformation("Destination Resolver (Unresolved)");
        p.getSafetyNotes().add(SafetyAdvisory.noVerifiedAdvisoryFound());
        p.setTypicalTripDuration("Unspecified");
        p.setTransportAdvice("Please specify a recognized town or city in India to calculate precise routes.");
        p.setFamilySuitability("Unknown");
        p.setBudgetNotes("Estimated");
        p.setBestKnownFor("Unresolved");
        return p;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public List<String> getMajorHighlights() {
        return majorHighlights;
    }

    public void setMajorHighlights(List<String> majorHighlights) {
        this.majorHighlights = majorHighlights != null ? majorHighlights : new ArrayList<>();
    }

    public List<String> getAttractions() {
        return attractions;
    }

    public void setAttractions(List<String> attractions) {
        this.attractions = attractions != null ? attractions : new ArrayList<>();
    }

    public List<String> getHeritageHighlights() {
        return heritageHighlights;
    }

    public void setHeritageHighlights(List<String> heritageHighlights) {
        this.heritageHighlights = heritageHighlights != null ? heritageHighlights : new ArrayList<>();
    }

    public List<String> getNatureHighlights() {
        return natureHighlights;
    }

    public void setNatureHighlights(List<String> natureHighlights) {
        this.natureHighlights = natureHighlights != null ? natureHighlights : new ArrayList<>();
    }

    public List<String> getPhotographySpots() {
        return photographySpots;
    }

    public void setPhotographySpots(List<String> photographySpots) {
        this.photographySpots = photographySpots != null ? photographySpots : new ArrayList<>();
    }

    public List<String> getShoppingHighlights() {
        return shoppingHighlights;
    }

    public void setShoppingHighlights(List<String> shoppingHighlights) {
        this.shoppingHighlights = shoppingHighlights != null ? shoppingHighlights : new ArrayList<>();
    }

    public List<String> getCulinaryHighlights() {
        return culinaryHighlights;
    }

    public void setCulinaryHighlights(List<String> culinaryHighlights) {
        this.culinaryHighlights = culinaryHighlights != null ? culinaryHighlights : new ArrayList<>();
    }

    public List<String> getLocalSpecialities() {
        return localSpecialities;
    }

    public void setLocalSpecialities(List<String> localSpecialities) {
        this.localSpecialities = localSpecialities != null ? localSpecialities : new ArrayList<>();
    }

    public List<String> getSuggestedActivities() {
        return suggestedActivities;
    }

    public void setSuggestedActivities(List<String> suggestedActivities) {
        this.suggestedActivities = suggestedActivities != null ? suggestedActivities : new ArrayList<>();
    }

    public String getTypicalTripDuration() {
        return typicalTripDuration;
    }

    public void setTypicalTripDuration(String typicalTripDuration) {
        this.typicalTripDuration = typicalTripDuration;
    }

    public List<String> getLocalTravelAdvice() {
        return localTravelAdvice;
    }

    public void setLocalTravelAdvice(List<String> localTravelAdvice) {
        this.localTravelAdvice = localTravelAdvice != null ? localTravelAdvice : new ArrayList<>();
    }

    public String getTransportAdvice() {
        return transportAdvice;
    }

    public void setTransportAdvice(String transportAdvice) {
        this.transportAdvice = transportAdvice;
    }

    public String getFamilySuitability() {
        return familySuitability;
    }

    public void setFamilySuitability(String familySuitability) {
        this.familySuitability = familySuitability;
    }

    public String getBudgetNotes() {
        return budgetNotes;
    }

    public void setBudgetNotes(String budgetNotes) {
        this.budgetNotes = budgetNotes;
    }

    public String getBestKnownFor() {
        return bestKnownFor;
    }

    public void setBestKnownFor(String bestKnownFor) {
        this.bestKnownFor = bestKnownFor;
    }

    public List<SafetyAdvisory> getSafetyNotes() {
        return safetyNotes;
    }

    public void setSafetyNotes(List<SafetyAdvisory> safetyNotes) {
        this.safetyNotes = safetyNotes != null ? safetyNotes : new ArrayList<>();
    }

    public String getSourceInformation() {
        return sourceInformation;
    }

    public void setSourceInformation(String sourceInformation) {
        this.sourceInformation = sourceInformation;
    }

    public String getKnowledgeStatus() {
        return knowledgeStatus;
    }

    public void setKnowledgeStatus(String knowledgeStatus) {
        this.knowledgeStatus = knowledgeStatus;
    }
}
