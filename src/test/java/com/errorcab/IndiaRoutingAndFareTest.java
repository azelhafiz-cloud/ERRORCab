package com.errorcab;

import com.errorcab.copilot.destination.model.DestinationResult;
import com.errorcab.copilot.destination.service.DestinationIntelligenceService;
import com.errorcab.copilot.routing.model.RouteResult;
import com.errorcab.copilot.routing.service.LocalFallbackRoutingProvider;
import com.errorcab.model.CabType;
import com.errorcab.service.FareService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification of India-wide Routing & Java Fare Calculation.
 * Verifies that distances use road routing / 1.30x circuity factor,
 * and fares are strictly calculated via Java FareService polymorphic rules.
 */
public class IndiaRoutingAndFareTest {

    private LocalFallbackRoutingProvider localRouter;
    private DestinationIntelligenceService intelligenceService;
    private FareService fareService;

    @BeforeEach
    public void setUp() {
        localRouter = new LocalFallbackRoutingProvider();
        intelligenceService = new DestinationIntelligenceService();
        fareService = FareService.getInstance();
    }

    @Test
    public void testHaversineWithRoadCircuityFactor() {
        // Kochi (9.9312, 76.2673) to Perinthalmanna (10.9760, 76.2254)
        // Straight line distance is ~116.5 km
        // Road circuity 1.30x should yield ~151 km
        RouteResult route = localRouter.calculateRoute(
                9.9312, 76.2673,
                10.9760, 76.2254,
                "Kochi", "Perinthalmanna"
        );

        assertNotNull(route);
        assertTrue(route.isRouteAvailable());
        assertTrue(route.getDistanceKm() > 130.0, "Road distance should include circuity factor (at least 130 km)");
        assertTrue(route.getDistanceKm() < 170.0, "Road distance should be reasonable (< 170 km)");
        assertTrue(route.getDurationMinutes() > 120, "Should take more than 2 hours");
        assertEquals("LOCAL_HAVERSINE_ESTIMATE", route.getMethod());
        assertTrue(route.isFallbackEstimate());
    }

    @Test
    public void testPolymorphicFareCalculationRules() {
        // Distance: 100.0 km
        double distanceKm = 100.0;

        // Economy: ₹50 base + ₹14/km = ₹1,450
        double economyFare = fareService.calculateFare(CabType.ECONOMY, distanceKm);
        assertEquals(1450.0, economyFare, 1.0, "Economy fare for 100 km should be ₹1,450 (50 + 14 * 100)");

        // Premium: ₹80 base + ₹20/km = ₹2,080
        double premiumFare = fareService.calculateFare(CabType.PREMIUM, distanceKm);
        assertEquals(2080.0, premiumFare, 1.0, "Premium fare for 100 km should be ₹2,080 (80 + 20 * 100)");

        // SUV: ₹100 base + ₹25/km = ₹2,600
        double suvFare = fareService.calculateFare(CabType.SUV, distanceKm);
        assertEquals(2600.0, suvFare, 1.0, "SUV fare for 100 km should be ₹2,600 (100 + 25 * 100)");
    }

    @Test
    public void testDestinationIntelligenceFaresConsistency() {
        DestinationResult perinthalmanna = intelligenceService.resolveDestination("Perinthalmanna");
        assertNotNull(perinthalmanna);
        assertTrue(perinthalmanna.isResolved());

        RouteResult route = intelligenceService.calculateRoute("Kochi", perinthalmanna);
        assertNotNull(route);
        assertTrue(route.isRouteAvailable());

        Map<CabType, Double> fares = intelligenceService.calculateFares(route.getDistanceKm());
        assertNotNull(fares);
        assertTrue(fares.containsKey(CabType.ECONOMY));
        assertTrue(fares.containsKey(CabType.PREMIUM));
        assertTrue(fares.containsKey(CabType.SUV));

        // Premium must cost more than Economy, and SUV more than Premium
        assertTrue(fares.get(CabType.PREMIUM) > fares.get(CabType.ECONOMY));
        assertTrue(fares.get(CabType.SUV) > fares.get(CabType.PREMIUM));
    }
}
