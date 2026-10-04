package com.errorcab;

import com.errorcab.copilot.destination.model.DestinationProfile;
import com.errorcab.copilot.destination.model.DestinationResult;
import com.errorcab.copilot.destination.service.DestinationKnowledgeBase;
import com.errorcab.copilot.destination.service.DestinationResolver;
import com.errorcab.copilot.model.CopilotContext;
import com.errorcab.copilot.model.CopilotResponse;
import com.errorcab.copilot.model.CopilotTripRequest;
import com.errorcab.copilot.provider.RuleEngineCopilotProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification of India-wide Destination Resolution.
 * Tests resolution of Perinthalmanna, Delhi, Mumbai, Jaipur, and strict handling of unresolved destinations.
 * Confirms system never silently uses a random fallback destination.
 */
public class IndiaDestinationResolutionTest {

    private DestinationResolver destinationResolver;
    private RuleEngineCopilotProvider copilotProvider;

    @BeforeEach
    public void setUp() {
        destinationResolver = new DestinationResolver(false);
        copilotProvider = new RuleEngineCopilotProvider();
    }

    @Test
    public void testPerinthalmannaResolution() {
        DestinationResult result = destinationResolver.resolveDestination("Perinthalmanna");
        assertNotNull(result, "Result should not be null for Perinthalmanna");
        assertTrue(result.isResolved(), "Perinthalmanna must resolve");
        assertEquals("Perinthalmanna", result.getNormalizedPlaceName());
        assertEquals("Malappuram", result.getDistrict());
        assertEquals("Kerala", result.getState());
        assertEquals("India", result.getCountry());
        assertEquals(10.9760, result.getLatitude(), 0.01);
        assertEquals(76.2254, result.getLongitude(), 0.01);

        DestinationProfile profile = DestinationKnowledgeBase.find("Perinthalmanna");
        assertNotNull(profile);
        assertTrue(profile.getMajorHighlights().stream().anyMatch(h -> h.contains("Angadipuram") || h.contains("Thirumandhamkunnu")));
    }

    @Test
    public void testMajorIndianMetrosResolution() {
        String[] metros = {"Delhi", "Mumbai", "Jaipur", "Agra", "Hyderabad", "Bengaluru", "Chennai", "Kolkata", "Goa", "Pune", "Amritsar", "Varanasi", "Mysuru", "Coimbatore"};

        for (String metro : metros) {
            DestinationResult result = destinationResolver.resolveDestination(metro);
            assertNotNull(result, "Resolution result should not be null for " + metro);
            assertTrue(result.isResolved(), metro + " must resolve successfully");
            assertNotNull(result.getState(), metro + " must have a resolved state");
            assertEquals("India", result.getCountry(), metro + " must have country India");
            assertTrue(result.getLatitude() > 0, metro + " latitude must be positive");
            assertTrue(result.getLongitude() > 0, metro + " longitude must be positive");
        }
    }

    @Test
    public void testKeralaDestinationsPreserved() {
        String[] keralaSpots = {"Fort Kochi", "Munnar", "Wayanad", "Kozhikode", "Alappuzha", "Varkala", "Thekkady"};

        for (String spot : keralaSpots) {
            DestinationResult result = destinationResolver.resolveDestination(spot);
            assertNotNull(result);
            assertTrue(result.isResolved());
            assertEquals("Kerala", result.getState());
        }
    }

    @Test
    public void testUnknownDestinationStrictHandlingWithoutRandomFallback() {
        String unknownQuery = "ZzUnknownPlaceX9988";
        DestinationResult result = destinationResolver.resolveDestination(unknownQuery);

        assertNotNull(result);
        assertFalse(result.isResolved(), "Unknown destination must NOT be resolved");

        // When generating a plan for an unknown destination, copilot must clearly state unresolved
        CopilotTripRequest request = new CopilotTripRequest(1, "Leisure", unknownQuery, "Half-day", "Moderate");
        CopilotContext context = new CopilotContext();
        context.setResolvedPickupLocation("Kochi");

        CopilotResponse response = copilotProvider.generatePlan(context, request);
        assertNotNull(response);
        assertFalse(response.isDestinationResolved(), "destinationResolved flag must be false");
        assertEquals("Couldn't confidently locate this destination. Try adding the district or state.", response.getResolutionErrorMessage());
        assertTrue(response.getSummary().contains("Couldn't confidently locate this destination. Try adding the district or state."));
        assertTrue(response.getItinerary().isEmpty(), "Itinerary must be empty for unresolved destination (no hallucinated stops)");
    }
}
