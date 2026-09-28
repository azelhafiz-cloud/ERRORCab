package com.errorcab;

import com.errorcab.config.AppConfig;
import com.errorcab.copilot.controller.AiCopilotController;
import com.errorcab.copilot.model.CopilotContext;
import com.errorcab.copilot.model.CopilotResponse;
import com.errorcab.copilot.model.CopilotRideSuggestion;
import com.errorcab.copilot.model.CopilotTripRequest;
import com.errorcab.copilot.model.ItineraryLeg;
import com.errorcab.copilot.provider.RuleEngineCopilotProvider;
import com.errorcab.copilot.service.AiCopilotService;
import com.errorcab.database.DatabaseManager;
import com.errorcab.database.DatabaseSeeder;
import com.errorcab.model.CabType;
import com.errorcab.service.SessionService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated tests for ERRORCab AI — Travel Copilot foundation.
 * Validates provider abstraction, local rule engine, context aggregation,
 * ERRORCab ride booking URL integration, and controller endpoints.
 */
public class AiCopilotTest {

    @BeforeAll
    public static void setup() {
        DatabaseManager.getInstance();
        DatabaseSeeder.seedIfEmpty();
    }

    @Test
    public void testRuleEngineCopilotProviderDirectly() {
        RuleEngineCopilotProvider provider = new RuleEngineCopilotProvider();
        assertEquals("ERRORCab Local Rule Engine (Offline Foundation)", provider.getProviderName());
        assertTrue(provider.isAvailable());

        CopilotTripRequest request = new CopilotTripRequest(
                1,
                "Leisure & Tourism",
                "Fort Kochi",
                "Half-day (4-5 hrs)",
                "Moderate (₹1,500 - ₹3,500)"
        );
        request.setInterests(List.of("Heritage & History", "Beaches & Sunset"));
        request.setFoodPreferences(List.of("Fresh Coastal Seafood", "Artisanal Cafes & Bakeries"));
        request.setActivityPreferences(List.of("Sightseeing & Landmarks", "Photography & Viewpoints"));

        CopilotContext context = new CopilotContext();
        context.setPassengerId(1);
        context.setPassengerName("Rahul Nair");
        context.setResolvedPickupLocation("Kakkanad");

        CopilotResponse response = provider.generatePlan(context, request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Fort Kochi", response.getDestination());
        assertFalse(response.getItinerary().isEmpty(), "Itinerary should contain legs");
        assertFalse(response.getRecommendedRides().isEmpty(), "Should contain suggested ERRORCab rides");
        assertFalse(response.getFoodRecommendations().isEmpty(), "Should contain food suggestions");
        assertFalse(response.getTravelTips().isEmpty(), "Should contain travel tips");

        // Verify connecting ERRORCab ride suggestion
        CopilotRideSuggestion ride = response.getRecommendedRides().get(0);
        assertEquals("Kakkanad", ride.getPickupLocation());
        assertEquals("Fort Kochi", ride.getDropoffLocation());
        assertTrue(ride.getDistanceKm() > 0, "Distance must be positive");
        assertTrue(ride.getEstimatedFare() > 0, "Fare must be positive");
        assertEquals(CabType.PREMIUM, ride.getRecommendedCabType(), "Moderate budget should map to PREMIUM cab");
        assertTrue(ride.getBookingUrl().startsWith("/booking.html"), "Booking URL must deep-link to booking flow");
        assertTrue(ride.getBookingUrl().contains("pickup=Kakkanad"));
        assertTrue(ride.getBookingUrl().contains("cabType=PREMIUM"));
    }

    @Test
    public void testContextAggregationUsingExistingDatabase() {
        AiCopilotService service = new AiCopilotService();
        CopilotContext context = service.buildContext(1); // Passenger ID 1 (Rahul Nair)

        assertNotNull(context);
        assertEquals(1, context.getPassengerId());
        assertNotNull(context.getPassengerName());
        assertNotNull(context.getResolvedPickupLocation(), "Should resolve default pickup location");
        assertNotNull(context.getAvailableCabClasses());
        assertTrue(context.getAvailableCabClasses().contains("ECONOMY"));
        assertTrue(context.getAvailableCabClasses().contains("PREMIUM"));
        assertTrue(context.getAvailableCabClasses().contains("SUV"));
    }

    @Test
    public void testServicePlanGenerationWithCustomPreferences() {
        AiCopilotService service = new AiCopilotService();

        CopilotTripRequest request = new CopilotTripRequest(
                1,
                "Shopping & Retail",
                "Edappally",
                "Full-day (8-10 hrs)",
                "Budget (₹500 - ₹1,500)"
        );
        request.setInterests(List.of("Shopping & Retail"));
        request.setFoodPreferences(List.of("Pure Vegetarian Delights"));

        CopilotResponse response = service.generateTravelPlan(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Edappally", response.getDestination());
        assertFalse(response.getItinerary().isEmpty());

        // Budget preference should recommend ECONOMY cab
        assertFalse(response.getRecommendedRides().isEmpty());
        CopilotRideSuggestion ride = response.getRecommendedRides().get(0);
        assertEquals(CabType.ECONOMY, ride.getRecommendedCabType());
        assertTrue(ride.getEstimatedFare() >= 80, "Minimum fare should apply");
    }

    @Test
    public void testCopilotControllerEndpoints() {
        AiCopilotService service = new AiCopilotService();
        SessionService sessionService = new SessionService();
        AiCopilotController controller = new AiCopilotController(service, sessionService);

        MockHttpServletRequest request = new MockHttpServletRequest();

        // 1. Status endpoint
        ResponseEntity<?> statusRes = controller.getStatus();
        assertEquals(HttpStatus.OK, statusRes.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> statusMap = (Map<String, Object>) statusRes.getBody();
        assertNotNull(statusMap);
        assertEquals("ERRORCab AI — Travel Copilot", statusMap.get("feature"));
        assertEquals(true, statusMap.get("externalAiReady"));

        // 2. Schema endpoint
        ResponseEntity<?> schemaRes = controller.getSchema();
        assertEquals(HttpStatus.OK, schemaRes.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> schemaMap = (Map<String, Object>) schemaRes.getBody();
        assertNotNull(schemaMap);
        assertTrue(schemaMap.containsKey("purposes"));
        assertTrue(schemaMap.containsKey("popularDestinations"));
        assertTrue(schemaMap.containsKey("budgets"));

        // 3. Context endpoint
        ResponseEntity<?> contextRes = controller.getContext(1, request);
        assertEquals(HttpStatus.OK, contextRes.getStatusCode());
        CopilotContext context = (CopilotContext) contextRes.getBody();
        assertNotNull(context);
        assertEquals(1, context.getPassengerId());

        // 4. Plan endpoint
        CopilotTripRequest tripReq = new CopilotTripRequest(1, "Family Outing", "Vyttila", "Half-day", "Premium (₹3,500+)");
        ResponseEntity<?> planRes = controller.generatePlan(tripReq, request);
        assertEquals(HttpStatus.OK, planRes.getStatusCode());
        CopilotResponse plan = (CopilotResponse) planRes.getBody();
        assertNotNull(plan);
        assertTrue(plan.isSuccess());
        assertEquals("Vyttila", plan.getDestination());
        assertEquals(CabType.SUV, plan.getRecommendedRides().get(0).getRecommendedCabType());
    }
}
