package com.errorcab;

import com.errorcab.copilot.destination.model.DestinationProfile;
import com.errorcab.copilot.destination.model.SafetyAdvisory;
import com.errorcab.copilot.destination.service.DestinationKnowledgeBase;
import com.errorcab.copilot.destination.service.DestinationResolver;
import com.errorcab.copilot.model.CopilotContext;
import com.errorcab.copilot.model.CopilotResponse;
import com.errorcab.copilot.model.CopilotTripRequest;
import com.errorcab.copilot.model.ItineraryLeg;
import com.errorcab.copilot.provider.RuleEngineCopilotProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit and integration tests for the Destination Intelligence Layer.
 * Verifies Knowledge Base depth, resolver pipeline, safe limited fallback for unknown destinations,
 * verified safety advisories, curated culinary structure, budget breakdown, and smart day balance.
 */
public class DestinationIntelligenceTest {

    @Test
    public void testKnowledgeBaseCoverage() {
        // Verify key Kerala destinations are registered
        assertTrue(DestinationKnowledgeBase.getAll().size() >= 20, "Knowledge base should cover 20+ destinations");

        List<String> expectedDestinations = List.of(
                "Fort Kochi", "Kozhikode", "Wayanad", "Vagamon", "Munnar",
                "Alappuzha", "Varkala", "Kovalam", "Bekal", "Kannur",
                "Thrissur", "Kumarakom", "Thekkady", "Idukki", "Kollam",
                "Thiruvananthapuram", "Edappally", "Kakkanad", "Vyttila", "Goa"
        );

        for (String dest : expectedDestinations) {
            DestinationProfile profile = DestinationKnowledgeBase.find(dest);
            assertNotNull(profile, "Profile must exist for " + dest);
            assertNotNull(profile.getDestinationName());
            assertNotNull(profile.getRegion(), "Region must be set for " + dest);
            assertFalse(profile.getMajorHighlights().isEmpty(), "majorHighlights must not be empty for " + dest);
            assertFalse(profile.getAttractions().isEmpty(), "attractions must not be empty for " + dest);
            assertFalse(profile.getCulinaryHighlights().isEmpty(), "culinaryHighlights must not be empty for " + dest);
            assertFalse(profile.getSafetyNotes().isEmpty(), "safetyNotes must not be empty for " + dest);
        }
    }

    @Test
    public void testDestinationResolverKnownDestinationsAndAliases() {
        DestinationResolver resolver = new DestinationResolver(false);

        // Exact match
        DestinationProfile kochi = resolver.resolve("Fort Kochi");
        assertNotNull(kochi);
        assertTrue(kochi.getDestinationName().contains("Fort Kochi"));
        assertEquals("Central Kerala", kochi.getRegion());

        // Alias match: Calicut -> Kozhikode
        DestinationProfile calicut = resolver.resolve("Calicut");
        assertNotNull(calicut);
        assertTrue(calicut.getDestinationName().contains("Kozhikode"));
        assertEquals("Malabar Coast", calicut.getRegion());
        assertFalse(calicut.getCulinaryHighlights().isEmpty());

        // Case-insensitive match
        DestinationProfile wayanad = resolver.resolve("wayanad");
        assertNotNull(wayanad);
        assertTrue(wayanad.getDestinationName().contains("Wayanad"));
        assertEquals("North Malabar High Ranges", wayanad.getRegion());
    }

    @Test
    public void testDestinationResolverUnknownDestinationFactualFallback() {
        DestinationResolver resolver = new DestinationResolver(false);

        // Unknown destination: must NOT throw, must NOT hallucinate fake hazards
        DestinationProfile unknown = resolver.resolve("NonExistentPlaceXYZ");
        assertNotNull(unknown);
        assertEquals("NonExistentPlaceXYZ", unknown.getDestinationName());
        assertTrue("LIMITED_FRAMEWORK".equals(unknown.getKnowledgeStatus()) || "UNRESOLVED".equals(unknown.getKnowledgeStatus()));

        // Safety advisory must be the strict factual default with verified source
        assertFalse(unknown.getSafetyNotes().isEmpty());
        SafetyAdvisory advisory = unknown.getSafetyNotes().get(0);
        assertTrue(advisory.getText().contains("No verified destination-specific"));
        assertEquals("Verified Travel Advisory Database", advisory.getSource());
    }

    @Test
    public void testSafetyAdvisoriesSourceAttribution() {
        for (DestinationProfile profile : DestinationKnowledgeBase.getAll().values()) {
            for (SafetyAdvisory adv : profile.getSafetyNotes()) {
                assertNotNull(adv.getText(), "Advisory text cannot be null");
                assertFalse(adv.getText().isBlank(), "Advisory text cannot be blank");
                assertNotNull(adv.getSource(), "Advisory must have a source");
                assertFalse(adv.getSource().isBlank(), "Advisory source cannot be blank");
                assertNotNull(adv.getCategory(), "Advisory category cannot be null");
            }
        }
    }

    @Test
    public void testRuleEngineCopilotProviderGeneratesEnrichedIntelligence() {
        RuleEngineCopilotProvider provider = new RuleEngineCopilotProvider();

        CopilotTripRequest request = new CopilotTripRequest(
                1,
                "Culinary & Food Tour",
                "Kozhikode",
                "Full-day (8-10 hrs)",
                "Moderate (₹1,500 - ₹3,500)"
        );
        request.setInterests(List.of("Heritage & History", "Architecture & Photography"));
        request.setFoodPreferences(List.of("Fresh Coastal Seafood", "Artisanal Cafes & Bakeries"));

        CopilotContext context = new CopilotContext();
        context.setPassengerId(1);
        context.setPassengerName("Nithin V");
        context.setResolvedPickupLocation("Kozhikode Railway Station");

        CopilotResponse response = provider.generatePlan(context, request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Kozhikode", response.getDestination());
        assertEquals("SMART_OFFLINE", response.getAssistanceType());

        // 1. Destination Profile attached
        assertNotNull(response.getDestinationProfile());
        assertTrue(response.getDestinationProfile().getDestinationName().contains("Kozhikode"));

        // 2. Curated Culinary (4-part model)
        assertNotNull(response.getCuratedCulinary());
        assertNotNull(response.getCuratedCulinary().getSignatureFood());
        assertNotNull(response.getCuratedCulinary().getLocalCafeCulture());
        assertNotNull(response.getCuratedCulinary().getTraditionalCuisine());
        assertFalse(response.getCuratedCulinary().getRecommendedExperiences().isEmpty());

        // 3. Smart Day Balance
        assertNotNull(response.getDayBalance());
        int totalPercent = response.getDayBalance().getSightseeingPercent()
                + response.getDayBalance().getWalkingPercent()
                + response.getDayBalance().getFoodPercent()
                + response.getDayBalance().getRelaxationPercent()
                + response.getDayBalance().getTravelPercent();
        assertEquals(100, totalPercent, "Day balance percentages must sum to 100%");

        // 4. Budget Intelligence
        assertNotNull(response.getBudgetBreakdown());
        assertTrue(response.getBudgetBreakdown().getTotalBudget() > 0);
        assertTrue(response.getBudgetBreakdown().getEstimatedCabFare() > 0);
        assertTrue(response.getBudgetBreakdown().getEstimatedFoodCost() > 0);

        // 5. Trip Readiness Checklist
        assertNotNull(response.getTripReadiness());
        assertFalse(response.getTripReadiness().isEmpty());

        // 6. Itinerary Legs with Personalization Explanation & Approx Cost
        assertFalse(response.getItinerary().isEmpty());
        for (ItineraryLeg leg : response.getItinerary()) {
            assertNotNull(leg.getTitle());
            assertNotNull(leg.getLocationName());
            assertNotNull(leg.getTimeSlot());
            assertNotNull(leg.getRecommendationReason(), "Leg should explain why ERRORCab recommends it");
            assertNotNull(leg.getApproxCost(), "Leg should have approx cost");
        }

        // 7. Verified Safety Advisories in response
        assertFalse(response.getSafetyAdvisories().isEmpty());
        for (SafetyAdvisory sa : response.getSafetyAdvisories()) {
            assertNotNull(sa.getSource());
            assertFalse(sa.getSource().isBlank());
        }
    }
}
