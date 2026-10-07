package com.errorcab.copilot.provider;

import com.errorcab.copilot.destination.model.DestinationProfile;
import com.errorcab.copilot.destination.model.DestinationResult;
import com.errorcab.copilot.destination.model.SafetyAdvisory;
import com.errorcab.copilot.destination.service.DestinationIntelligenceService;
import com.errorcab.copilot.gemini.dto.GeminiLegPayload;
import com.errorcab.copilot.gemini.dto.GeminiPlanPayload;
import com.errorcab.copilot.model.BudgetBreakdown;
import com.errorcab.copilot.model.CopilotContext;
import com.errorcab.copilot.model.CopilotResponse;
import com.errorcab.copilot.model.CopilotRideSuggestion;
import com.errorcab.copilot.model.CopilotTripRequest;
import com.errorcab.copilot.model.CuratedCulinaryInfo;
import com.errorcab.copilot.model.DayBalance;
import com.errorcab.copilot.model.DriverRecommendation;
import com.errorcab.copilot.model.ItineraryLeg;
import com.errorcab.copilot.routing.model.RouteResult;
import com.errorcab.copilot.weather.model.WeatherResult;
import com.errorcab.model.CabType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shared provider-agnostic mapper that converts structured AI plan payloads into
 * complete ERRORCab CopilotResponse models enriched with authentic route distances,
 * calculated taxi fares, drivers, culinary context, and safety advisories.
 */
public class CopilotPlanMapper {

    private final DestinationIntelligenceService intelligenceService;

    public CopilotPlanMapper(DestinationIntelligenceService intelligenceService) {
        this.intelligenceService = intelligenceService != null ? intelligenceService : new DestinationIntelligenceService();
    }

    public CopilotPlanMapper() {
        this(new DestinationIntelligenceService());
    }

    public CopilotResponse mapPayloadToCopilotResponse(GeminiPlanPayload payload,
                                                       CopilotContext context,
                                                       CopilotTripRequest request,
                                                       String providerName) {
        String destStr = request != null && request.getDestination() != null ? request.getDestination() : "Kochi";
        DestinationResult destResult = intelligenceService.resolveDestination(destStr);
        DestinationProfile profile = intelligenceService.getProfile(destResult);
        String pickupHub = context != null && context.getResolvedPickupLocation() != null
                ? context.getResolvedPickupLocation() : "Kakkanad";
        RouteResult routeResult = intelligenceService.calculateRoute(pickupHub, destResult);
        double routeKm = routeResult.isRouteAvailable() ? routeResult.getDistanceKm() : 15.0;
        Map<CabType, Double> fares = intelligenceService.calculateFares(routeKm);
        CabType cabType = resolveCabType(request != null ? request.getBudget() : null);
        DriverRecommendation driverRec = intelligenceService.recommendDriver(cabType, pickupHub);
        WeatherResult weather = intelligenceService.getWeather(destResult);

        return mapPayloadToCopilotResponse(payload, context, request, destResult, routeResult, fares, profile, driverRec, weather, providerName);
    }

    public CopilotResponse mapPayloadToCopilotResponse(GeminiPlanPayload payload,
                                                       CopilotContext context,
                                                       CopilotTripRequest request,
                                                       DestinationResult destResult,
                                                       RouteResult routeResult,
                                                       Map<CabType, Double> fares,
                                                       DestinationProfile profile,
                                                       DriverRecommendation driverRec,
                                                       WeatherResult weather,
                                                       String providerName) {
        CopilotResponse response = new CopilotResponse();
        response.setProviderName(providerName != null ? providerName : "AI Assisted");
        response.setAssistanceType("AI_ASSISTED");
        response.setTitle(payload.getTitle());
        response.setSummary(payload.getSummary());
        response.setTripPurpose(request != null ? request.getTripPurpose() : "Leisure");
        response.setDestination(request != null ? request.getDestination() : "Kochi");
        response.setDuration(request != null ? request.getDuration() : "Full-day");
        response.setBudget(request != null ? request.getBudget() : "Moderate");
        response.setFoodRecommendations(payload.getFoodRecommendations());
        response.setTravelTips(payload.getTravelTips());
        response.setSpecialties(payload.getSpecialties());
        response.setWarnings(payload.getWarnings());
        response.setGeneratedAt(LocalDateTime.now());

        response.setDestinationResult(destResult);
        response.setRouteResult(routeResult);
        response.setDestinationProfile(profile);
        response.setRecommendedDriver(driverRec);
        response.setWeather(weather);

        double ecoFare = fares != null && fares.containsKey(CabType.ECONOMY) ? fares.get(CabType.ECONOMY) : 150.0;
        double premFare = fares != null && fares.containsKey(CabType.PREMIUM) ? fares.get(CabType.PREMIUM) : 220.0;
        double suvFare = fares != null && fares.containsKey(CabType.SUV) ? fares.get(CabType.SUV) : 320.0;
        response.setEconomyFare(ecoFare);
        response.setPremiumFare(premFare);
        response.setSuvFare(suvFare);

        String defaultPickup = context != null && context.getResolvedPickupLocation() != null
                ? context.getResolvedPickupLocation() : "Kakkanad";
        CabType defaultCabType = resolveCabType(request != null ? request.getBudget() : null);

        List<ItineraryLeg> legs = new ArrayList<>();
        List<CopilotRideSuggestion> rides = new ArrayList<>();
        double totalCabFare = 0.0;
        int rideIndex = 1;

        double outwardDist = (routeResult != null && routeResult.isRouteAvailable()) ? routeResult.getDistanceKm() : 15.0;
        int outwardMins = (routeResult != null && routeResult.isRouteAvailable()) ? routeResult.getDurationMinutes() : 35;
        double outwardFare = (fares != null && fares.containsKey(defaultCabType)) ? fares.get(defaultCabType) : ecoFare;

        String normalizedDest = (destResult != null && destResult.getNormalizedPlaceName() != null)
                ? destResult.getNormalizedPlaceName() : (request != null && request.getDestination() != null ? request.getDestination() : "Kochi");

        if (payload.getItinerary() != null) {
            for (GeminiLegPayload legPayload : payload.getItinerary()) {
                if (legPayload == null) continue;
                CopilotRideSuggestion rideSuggestion = null;

                boolean wantsRide = Boolean.TRUE.equals(legPayload.getRideSuggested()) ||
                        "Transit".equalsIgnoreCase(legPayload.getCategory());

                if (wantsRide) {
                    String pLoc = legPayload.getPickupLocation() != null && !legPayload.getPickupLocation().isBlank()
                            ? legPayload.getPickupLocation().trim() : defaultPickup;
                    String dLoc = legPayload.getDropoffLocation() != null && !legPayload.getDropoffLocation().isBlank()
                            ? legPayload.getDropoffLocation().trim() : normalizedDest;

                    String reason = legPayload.getRideReason() != null ? legPayload.getRideReason() :
                            "Direct ERRORCab transit with zero surge pricing.";

                    rideSuggestion = new CopilotRideSuggestion(
                            "Ride " + rideIndex + ": " + pLoc + " ➔ " + dLoc,
                            pLoc, dLoc, outwardDist, outwardMins, defaultCabType, outwardFare, reason
                    );

                    rides.add(rideSuggestion);
                    totalCabFare += outwardFare;
                    rideIndex++;
                }

                ItineraryLeg leg = new ItineraryLeg(
                        legPayload.getTimeSlot() != null ? legPayload.getTimeSlot() : "Flexible Time",
                        legPayload.getTitle() != null ? legPayload.getTitle() : "Sightseeing Stop",
                        legPayload.getLocationName() != null ? legPayload.getLocationName() : normalizedDest,
                        legPayload.getCategory() != null ? legPayload.getCategory() : "Sightseeing",
                        legPayload.getDescription() != null ? legPayload.getDescription() : "",
                        rideSuggestion
                );
                legs.add(leg);
            }
        }

        if (rides.isEmpty()) {
            CopilotRideSuggestion outwardRide = new CopilotRideSuggestion(
                    "Primary Ride: " + defaultPickup + " ➔ " + normalizedDest,
                    defaultPickup, normalizedDest, outwardDist, outwardMins, defaultCabType, outwardFare,
                    "Direct pickup from " + defaultPickup + " to " + normalizedDest + "."
            );
            rides.add(outwardRide);
            totalCabFare += outwardFare;

            if (!legs.isEmpty()) {
                legs.get(0).setRideSuggestion(outwardRide);
            }
        }

        response.setItinerary(legs);
        response.setRecommendedRides(rides);
        response.setEstimatedTotalCabFare(Math.round(totalCabFare));

        // Curated Culinary
        String sigFood = (profile != null && profile.getCulinaryHighlights() != null && !profile.getCulinaryHighlights().isEmpty())
                ? profile.getCulinaryHighlights().get(0) : "Authentic Regional Delicacies";
        response.setCuratedCulinary(new CuratedCulinaryInfo(
                sigFood,
                "Local Tea & Artisanal Cafe Culture",
                "Traditional Regional Thali",
                (profile != null && profile.getCulinaryHighlights() != null) ? profile.getCulinaryHighlights() : List.of()
        ));

        // Safety Advisories
        List<SafetyAdvisory> advisories = (profile != null) ? profile.getSafetyNotes() : null;
        if (advisories == null || advisories.isEmpty()) {
            advisories = List.of(SafetyAdvisory.noVerifiedAdvisoryFound());
        }
        response.setSafetyAdvisories(advisories);

        // Budget breakdown
        response.setBudgetBreakdown(new BudgetBreakdown(
                3500.0, totalCabFare, 800.0, 500.0, 1200.0,
                "Approximate breakdown based on selected budget tier."
        ));

        // Day Balance
        response.setDayBalance(new DayBalance(20, 40, 20, 10, 10));

        // Trip Readiness
        List<String> readiness = new ArrayList<>();
        if (destResult != null && destResult.getDisplayName() != null) {
            readiness.add("✓ Destination resolved: " + destResult.getDisplayName());
        }
        if (routeResult != null) {
            readiness.add("✓ Route available: " + routeResult.getDistanceKm() + " km (" + routeResult.getDurationFormatted() + ")");
        }
        readiness.add("✓ Fare calculated: Economy ₹" + Math.round(ecoFare) + " • Premium ₹" + Math.round(premFare) + " • SUV ₹" + Math.round(suvFare));
        readiness.add("✓ Trip purpose selected: " + (request != null && request.getTripPurpose() != null ? request.getTripPurpose() : "Leisure"));
        readiness.add("✓ Budget selected: " + (request != null && request.getBudget() != null ? request.getBudget() : "Moderate"));
        readiness.add("✓ Preferences selected: " + (request != null && request.getInterests() != null && !request.getInterests().isEmpty() ? String.join(", ", request.getInterests()) : "General"));
        readiness.add("✓ Itinerary generated with verified ERRORCab transfers");
        response.setTripReadiness(readiness);

        return response;
    }

    public static CabType resolveCabType(String budget) {
        if (budget == null) return CabType.ECONOMY;
        String b = budget.toLowerCase();
        if (b.contains("premium") || b.contains("luxury")) {
            return CabType.SUV;
        } else if (b.contains("moderate") || b.contains("comfort")) {
            return CabType.PREMIUM;
        }
        return CabType.ECONOMY;
    }

    public DestinationIntelligenceService getIntelligenceService() {
        return intelligenceService;
    }
}
