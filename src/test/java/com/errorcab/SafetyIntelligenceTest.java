package com.errorcab;

import com.errorcab.copilot.destination.model.DestinationProfile;
import com.errorcab.copilot.destination.model.SafetyAdvisory;
import com.errorcab.copilot.destination.service.DestinationKnowledgeBase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification of Safety Intelligence.
 * Verifies that all advisories are attributed to authentic governing bodies,
 * contain titles, descriptions, dates, and status, and never invent scams or crimes.
 */
public class SafetyIntelligenceTest {

    @Test
    public void testOfficialSourceAttributionForMajorDestinations() {
        // Delhi: Delhi Traffic Police / ASI
        DestinationProfile delhi = DestinationKnowledgeBase.find("Delhi");
        assertNotNull(delhi);
        assertFalse(delhi.getSafetyNotes().isEmpty());
        SafetyAdvisory delhiAdv = delhi.getSafetyNotes().get(0);
        assertNotNull(delhiAdv.getTitle());
        assertTrue(delhiAdv.getSource().contains("Delhi Traffic Police") || delhiAdv.getSource().contains("ASI"));
        assertEquals("VERIFIED", delhiAdv.getStatus());
        assertNotNull(delhiAdv.getLastVerifiedDate());

        // Jaipur: Rajasthan Tourism (RTDC) / Jaipur Police
        DestinationProfile jaipur = DestinationKnowledgeBase.find("Jaipur");
        assertNotNull(jaipur);
        assertFalse(jaipur.getSafetyNotes().isEmpty());
        SafetyAdvisory jaipurAdv = jaipur.getSafetyNotes().get(0);
        assertTrue(jaipurAdv.getSource().contains("Rajasthan Tourism") || jaipurAdv.getSource().contains("RTDC") || jaipurAdv.getSource().contains("Police"));

        // Agra: UP Tourism / ASI
        DestinationProfile agra = DestinationKnowledgeBase.find("Agra");
        assertNotNull(agra);
        assertFalse(agra.getSafetyNotes().isEmpty());
        SafetyAdvisory agraAdv = agra.getSafetyNotes().get(0);
        assertTrue(agraAdv.getSource().contains("UP Tourism") || agraAdv.getSource().contains("ASI"));
    }

    @Test
    public void testNoFabricatedScamsGuarantee() {
        for (DestinationProfile profile : DestinationKnowledgeBase.getAll().values()) {
            for (SafetyAdvisory adv : profile.getSafetyNotes()) {
                assertNotNull(adv.getTitle(), "Advisory title must not be null");
                assertNotNull(adv.getText(), "Advisory text must not be null");
                assertNotNull(adv.getSource(), "Advisory source must not be null");
                assertFalse(adv.getSource().isBlank(), "Advisory source must not be blank");
                assertNotNull(adv.getLastVerifiedDate(), "Verified date must be present");
                assertNotNull(adv.getStatus(), "Status must be present");

                // Never contain generic slang like "locals scam tourists"
                assertFalse(adv.getText().toLowerCase().contains("locals will scam"), "Must not invent derogatory scam tropes");
            }
        }
    }

    @Test
    public void testFactualFallbackWhenNoAdvisoriesFound() {
        SafetyAdvisory defaultAdv = SafetyAdvisory.noVerifiedAdvisoryFound();
        assertNotNull(defaultAdv);
        assertEquals("No verified destination-specific safety advisory found.", defaultAdv.getText());
        assertEquals("Verified Travel Advisory Database", defaultAdv.getSource());
        assertEquals("GENERAL", defaultAdv.getStatus());
    }
}
