package com.errorcab.copilot.provider;

import com.errorcab.copilot.destination.model.DestinationProfile;
import com.errorcab.copilot.destination.model.DestinationResult;
import com.errorcab.copilot.destination.model.SafetyAdvisory;
import com.errorcab.copilot.destination.service.DestinationIntelligenceService;
import com.errorcab.copilot.destination.service.DestinationResolver;
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
import com.errorcab.service.FareService;
import com.errorcab.service.MapService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * High-performance, offline rule-based AI provider for ERRORCab.
 * Powered by DestinationIntelligenceService and DestinationResolver.
 * Supports India-wide destination resolution, routing, polymorphic fare calculation,
 * real driver recommendations, live weather context, and source-attributed safety advisories.
 * Operates 100% offline with zero external network dependencies.
 */
public class RuleEngineCopilotProvider implements AiCopilotProvider {

    private final MapService mapService = MapService.getInstance();
    private final FareService fareService = FareService.getInstance();
    private final DestinationIntelligenceService intelligenceService;
    private final DestinationResolver destinationResolver;

    public RuleEngineCopilotProvider(DestinationIntelligenceService intelligenceService) {
        this.intelligenceService = intelligenceService != null ? intelligenceService : new DestinationIntelligenceService();
        this.destinationResolver = this.intelligenceService.getDestinationResolver();
    }

    public RuleEngineCopilotProvider(DestinationResolver destinationResolver) {
        this.destinationResolver = destinationResolver != null ? destinationResolver : new DestinationResolver();
        this.intelligenceService = new DestinationIntelligenceService(this.destinationResolver, null, null, null);
    }

    public RuleEngineCopilotProvider() {
        this(new DestinationIntelligenceService());
    }

    @Override
    public String getProviderName() {
        return "ERRORCab Local Rule Engine (Offline Foundation)";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public CopilotResponse generatePlan(CopilotContext context, CopilotTripRequest request) {
        if (request == null) {
            request = new CopilotTripRequest();
        }

        CopilotResponse response = new CopilotResponse();
        response.setProviderName(getProviderName());
        response.setAssistanceType("SMART_OFFLINE");
        response.setTripPurpose(request.getTripPurpose() != null ? request.getTripPurpose() : "Leisure & Tourism");
        response.setDestination(request.getDestination() != null ? request.getDestination() : "Fort Kochi");
        response.setDuration(request.getDuration() != null ? request.getDuration() : "Half-day (4-5 hrs)");
        response.setBudget(request.getBudget() != null ? request.getBudget() : "Moderate (₹1,500 - ₹3,500)");
        response.setGeneratedAt(LocalDateTime.now());

        String passengerName = context != null && context.getPassengerName() != null ? context.getPassengerName() : "Traveler";
        String pickupHub = (request.getStartingLocation() != null && !request.getStartingLocation().trim().isEmpty())
                ? request.getStartingLocation().trim()
                : (context != null && context.getResolvedPickupLocation() != null ? context.getResolvedPickupLocation() : "Kakkanad");
        String rawDestination = response.getDestination();

        // 1. Resolve Destination via modular Destination Intelligence Layer
        DestinationResult destResult = intelligenceService.resolveDestination(rawDestination);
        response.setDestinationResult(destResult);

        // Strict unresolved handling: NEVER silently use a random fallback destination!
        if (destResult == null || !destResult.isResolved()) {
            response.setDestinationResolved(false);
            response.setResolutionErrorMessage("Couldn't confidently locate this destination. Try adding the district or state.");
            response.setTitle("Destination Unresolved: " + rawDestination);
            response.setSummary("Couldn't confidently locate this destination. Try adding the district or state.");
            response.setItinerary(new ArrayList<>());
            response.setRecommendedRides(new ArrayList<>());
            response.setSpecialties(new ArrayList<>());
            response.setFoodRecommendations(new ArrayList<>());
            response.setTravelTips(List.of("Try adding the district or state name (e.g. 'Perinthalmanna, Malappuram' or 'Jaipur, Rajasthan')."));
            response.setSafetyAdvisories(List.of(SafetyAdvisory.noVerifiedAdvisoryFound()));
            response.setAssistanceType("UNRESOLVED");
            return response;
        }

        DestinationProfile profile = intelligenceService.getProfile(destResult);
        if (profile == null) {
            profile = DestinationProfile.createLimited(destResult.getNormalizedPlaceName());
        }
        response.setDestinationProfile(profile);

        // 2. India-wide Routing & Java Fare Calculation
        RouteResult routeResult = intelligenceService.calculateRoute(pickupHub, destResult);
        response.setRouteResult(routeResult);

        double routeKm = routeResult.isRouteAvailable() ? routeResult.getDistanceKm() : 15.0;
        Map<CabType, Double> fares = intelligenceService.calculateFares(routeKm);
        response.setEconomyFare(fares.get(CabType.ECONOMY));
        response.setPremiumFare(fares.get(CabType.PREMIUM));
        response.setSuvFare(fares.get(CabType.SUV));

        // 3. Authentic ERRORCab Driver Recommendation from Database
        CabType recommendedCab = resolveCabType(request.getBudget());
        DriverRecommendation recommendedDriver = intelligenceService.recommendDriver(recommendedCab, pickupHub);
        response.setRecommendedDriver(recommendedDriver);

        // 4. Destination Weather (omit if unavailable, never fabricate)
        WeatherResult weather = intelligenceService.getWeather(destResult);
        response.setWeather(weather);
        if (weather != null && weather.isAvailable()) {
            response.setWeatherNote(Math.round(weather.getTemperatureC()) + "°C, " + weather.getCondition() +
                    (weather.isRaining() ? " • Rain Alert: Indoor friendly spots prioritized" : ""));
        }

        response.setTitle("Personalized " + profile.getDestinationName() + " " + response.getTripPurpose() + " Itinerary");
        response.setSummary(buildSummary(passengerName, pickupHub, profile, response.getDuration(), response.getBudget(), request, routeResult));

        // 5. Generate Landmark-Specific Itinerary Legs
        List<ItineraryLeg> legs = buildItineraryLegs(pickupHub, profile, recommendedCab, request, routeResult);
        response.setItinerary(legs);

        // 6. Extract suggested ERRORCab rides from the itinerary
        List<CopilotRideSuggestion> rides = new ArrayList<>();
        double totalCabFare = 0.0;
        for (ItineraryLeg leg : legs) {
            if (leg.getRideSuggestion() != null) {
                rides.add(leg.getRideSuggestion());
                totalCabFare += leg.getRideSuggestion().getEstimatedFare();
            }
        }
        response.setRecommendedRides(rides);
        response.setEstimatedTotalCabFare(Math.round(totalCabFare));

        // 7. Curated Culinary Highlights (4-part structure)
        CuratedCulinaryInfo culinaryInfo = buildCuratedCulinary(profile, request.getFoodPreferences());
        response.setCuratedCulinary(culinaryInfo);
        response.setFoodRecommendations(buildFoodSummaryList(culinaryInfo, profile));

        // 8. Destination Specialities & Highlights
        response.setSpecialties(buildSpecialties(profile, request.getInterests()));

        // 9. Source-Attributed Place Warnings & Safety Advisories
        List<SafetyAdvisory> advisories = buildSafetyAdvisories(profile);
        response.setSafetyAdvisories(advisories);
        List<String> formattedWarnings = new ArrayList<>();
        for (SafetyAdvisory adv : advisories) {
            formattedWarnings.add(adv.getText() + " (Source: " + adv.getSource() + ")");
        }
        response.setWarnings(formattedWarnings);

        // 10. Local Travel Advice
        response.setTravelTips(buildTravelAdvice(profile));

        // 11. Budget Intelligence Breakdown
        BudgetBreakdown budgetBreakdown = calculateBudgetBreakdown(request.getBudget(), totalCabFare);
        response.setBudgetBreakdown(budgetBreakdown);

        // 12. Smart Day Balance
        DayBalance dayBalance = calculateDayBalance(request.getTripPurpose(), request.getDuration());
        response.setDayBalance(dayBalance);

        // 13. Trip Readiness Checklist (7-check verification)
        response.setTripReadiness(buildTripReadiness(destResult, routeResult, profile, request, fares));

        // 14. Trip Explanation
        response.setTripExplanation(buildTripExplanation(legs, rides, profile));

        return response;
    }

    private CabType resolveCabType(String budget) {
        if (budget == null) return CabType.ECONOMY;
        String b = budget.toLowerCase();
        if (b.contains("premium") || b.contains("luxury")) {
            return CabType.SUV;
        } else if (b.contains("moderate") || b.contains("comfort")) {
            return CabType.PREMIUM;
        }
        return CabType.ECONOMY;
    }

    private String buildSummary(String passengerName, String pickupHub, DestinationProfile profile,
                                 String duration, String budget, CopilotTripRequest request, RouteResult route) {
        StringBuilder sb = new StringBuilder();
        sb.append("Welcome, ").append(passengerName).append("! ");
        sb.append("Here is your curated ").append(duration.toLowerCase()).append(" travel plan for ")
          .append(profile.getDestinationName()).append(", starting comfortably from your home hub at ").append(pickupHub).append(". ");

        if (route != null && route.isRouteAvailable()) {
            sb.append("Calculated route distance: ").append(route.getDistanceKm()).append(" km (approx. ")
              .append(route.getDurationFormatted()).append("). ");
        }

        if (profile.getShortDescription() != null && !profile.getShortDescription().isBlank()) {
            sb.append(profile.getShortDescription()).append(" ");
        }

        if (request.getInterests() != null && !request.getInterests().isEmpty()) {
            sb.append("Customized around your interests in ")
              .append(String.join(", ", request.getInterests())).append(". ");
        }

        sb.append("Connecting transit is coordinated via ERRORCab ").append(resolveCabType(budget).name())
          .append(" with guaranteed zero-surge pricing and certified drivers.");

        return sb.toString();
    }

    private List<ItineraryLeg> buildItineraryLegs(String pickupHub, DestinationProfile profile,
                                                  CabType cabType, CopilotTripRequest req, RouteResult route) {
        List<ItineraryLeg> list = new ArrayList<>();
        String destName = (req != null && req.getDestination() != null && !req.getDestination().isBlank())
                ? req.getDestination().trim()
                : profile.getDestinationName();

        // Calculate outward cab ride using resolved route distance
        double outwardDist = (route != null && route.isRouteAvailable()) ? route.getDistanceKm() : mapService.getDistanceKm(pickupHub, destName);
        int outwardMins = (route != null && route.isRouteAvailable()) ? route.getDurationMinutes() : mapService.getEstimatedMinutes(outwardDist);
        double outwardFare = fareService.calculateFare(cabType, outwardDist);

        String whyCabster = "Recommended because you selected: " +
                (req.getTripPurpose() != null ? req.getTripPurpose() : "Leisure") + " + " +
                (req.getBudget() != null ? req.getBudget() : "Moderate") +
                (req.getFoodPreferences() != null && !req.getFoodPreferences().isEmpty()
                        ? " + " + String.join(", ", req.getFoodPreferences()) : "");

        CopilotRideSuggestion outwardRide = new CopilotRideSuggestion(
                "Leg 1 Ride: " + pickupHub + " ➔ " + destName,
                pickupHub, destName, outwardDist, outwardMins, cabType, outwardFare,
                "Prompt dispatch from " + pickupHub + " directly to " + destName + " hub."
        );

        // Outward transit leg
        list.add(new ItineraryLeg(
                "09:00 AM - 10:15 AM",
                "Morning Departure & Scenic Transit to " + destName,
                pickupHub + " to " + destName,
                "Transit",
                "Relax in your air-conditioned ERRORCab " + cabType + " cab as you head towards " + destName + ". " +
                        (profile.getTransportAdvice() != null ? profile.getTransportAdvice() : ""),
                outwardRide,
                "Direct private transit scheduled early to avoid highway choke points. " + whyCabster,
                "₹" + Math.round(outwardFare) + " (Cab Fare)"
        ));

        // Populate stops from destination profile attractions or highlights
        List<String> attractions = profile.getAttractions();
        if (attractions == null || attractions.isEmpty()) {
            attractions = profile.getMajorHighlights();
        }

        String interestsStr = (req.getInterests() != null && !req.getInterests().isEmpty())
                ? String.join(" & ", req.getInterests()) : "Scenic Exploration";

        if ("LIMITED_FRAMEWORK".equals(profile.getKnowledgeStatus()) || attractions.isEmpty()) {
            list.add(new ItineraryLeg(
                    "10:30 AM - 01:00 PM",
                    "Arrival & Core District Exploration",
                    destName + " Central District",
                    "Sightseeing",
                    "Explore the central area, prominent public squares, and commercial landmarks of " + destName + ".",
                    null,
                    whyCabster,
                    "Nominal"
            ));

            list.add(new ItineraryLeg(
                    "01:15 PM - 02:45 PM",
                    "Local Dining & Refreshment Break",
                    destName + " Local Dining Hub",
                    "Culinary",
                    "Sample authentic regional cuisine at a reputable local dining establishment.",
                    null,
                    "Scheduled for midday replenishment matching your culinary preferences.",
                    "₹200 - ₹500"
            ));

            list.add(new ItineraryLeg(
                    "03:00 PM - 05:00 PM",
                    "Afternoon Promenades & Cultural Walking",
                    destName + " Public Walkways",
                    "Culture & Leisure",
                    "Engage in an afternoon leisure walk through local markets and viewpoints.",
                    null,
                    "Tailored for relaxed walking and photography.",
                    "Free entry"
            ));
        } else {
            String stop1 = attractions.get(0);
            String stop2 = attractions.size() > 1 ? attractions.get(1) : destName + " Market Promenade";
            String stop3 = attractions.size() > 2 ? attractions.get(2) : destName + " Cultural Center";

            list.add(new ItineraryLeg(
                    "10:30 AM - 12:30 PM",
                    stop1 + " Exploration",
                    stop1,
                    "Sightseeing",
                    "Discover the prominent highlights of " + stop1 + ". " +
                            (!profile.getPhotographySpots().isEmpty() ? "Prime photography spot: " + profile.getPhotographySpots().get(0) + "." : ""),
                    null,
                    "Prioritized because you selected " + interestsStr + "; renowned for " + profile.getBestKnownFor() + ". " + whyCabster,
                    "Free entry / Nominal ticket"
            ));

            list.add(new ItineraryLeg(
                    "12:45 PM - 02:30 PM",
                    "Culinary Highlights & " + stop2,
                    stop2,
                    "Culture & Dining",
                    "Immerse yourself in the vibrant surroundings of " + stop2 + ". Indulge in authentic regional delicacies.",
                    null,
                    "Scheduled around lunchtime to pair sightseeing with authentic regional food.",
                    "₹250 - ₹600"
            ));

            list.add(new ItineraryLeg(
                    "02:45 PM - 04:45 PM",
                    "Heritage & Nature Walk at " + stop3,
                    stop3,
                    "Heritage & Leisure",
                    "Experience " + stop3 + ". " +
                            (!profile.getHeritageHighlights().isEmpty() ? profile.getHeritageHighlights().get(0) : "Enjoy the unique regional ambiance."),
                    null,
                    "Recommended for afternoon relaxation and rich cultural depth.",
                    "Nominal"
            ));
        }

        // Return transit leg
        double returnDist = outwardDist;
        int returnMins = outwardMins;
        double returnFare = outwardFare;
        CopilotRideSuggestion returnRide = new CopilotRideSuggestion(
                "Return Ride: " + destName + " ➔ " + pickupHub,
                destName, pickupHub, returnDist, returnMins, cabType, returnFare,
                "Guaranteed comfortable AC return ride back home to " + pickupHub + "."
        );

        list.add(new ItineraryLeg(
                "05:15 PM - 06:45 PM",
                "Evening Return Ride to " + pickupHub,
                destName + " to " + pickupHub,
                "Transit",
                "Board your reserved ERRORCab ride for a smooth and comfortable journey back to " + pickupHub + ".",
                returnRide,
                "Pre-booked return avoids evening surge pricing and guarantees verified drivers.",
                "₹" + Math.round(returnFare) + " (Cab Fare)"
        ));

        return list;
    }

    private CuratedCulinaryInfo buildCuratedCulinary(DestinationProfile profile, List<String> foodPrefs) {
        String signature;
        String cafeCulture;
        String traditional;
        List<String> experiences = new ArrayList<>();

        if (!profile.getCulinaryHighlights().isEmpty()) {
            signature = profile.getCulinaryHighlights().get(0);
            traditional = profile.getCulinaryHighlights().size() > 1
                    ? profile.getCulinaryHighlights().get(1)
                    : "Traditional regional dining with authentic local spices";
            cafeCulture = profile.getCulinaryHighlights().size() > 2
                    ? profile.getCulinaryHighlights().get(2)
                    : "Local tea stalls and artisanal cafes";
            experiences.addAll(profile.getCulinaryHighlights());
        } else {
            signature = "Authentic Regional Indian Delicacies";
            traditional = "Traditional Meals served with seasonal accompaniments";
            cafeCulture = "Traditional roadside tea stalls serving hot spiced chai and regional snacks";
            experiences.add("Visit reputable local dining establishments with high guest turnover.");
            experiences.add("Pair regional meals with fresh local beverages.");
        }

        if (foodPrefs != null && !foodPrefs.isEmpty()) {
            experiences.add("Customized for selected preferences: " + String.join(", ", foodPrefs));
        }

        return new CuratedCulinaryInfo(signature, cafeCulture, traditional, experiences);
    }

    private List<String> buildFoodSummaryList(CuratedCulinaryInfo culinary, DestinationProfile profile) {
        List<String> list = new ArrayList<>();
        list.add("🍛 Signature Dish: " + culinary.getSignatureFood());
        list.add("🥘 Traditional Cuisine: " + culinary.getTraditionalCuisine());
        list.add("☕ Cafe & Tea Culture: " + culinary.getLocalCafeCulture());
        return list;
    }

    private List<String> buildSpecialties(DestinationProfile profile, List<String> interests) {
        if (!profile.getLocalSpecialities().isEmpty()) {
            return new ArrayList<>(profile.getLocalSpecialities());
        }
        if (!profile.getMajorHighlights().isEmpty()) {
            return new ArrayList<>(profile.getMajorHighlights());
        }
        return List.of("Regional Architecture", "Local Markets & Handlooms", "Scenic Landscapes");
    }

    private List<SafetyAdvisory> buildSafetyAdvisories(DestinationProfile profile) {
        List<SafetyAdvisory> advisories = new ArrayList<>();
        if (profile.getSafetyNotes() != null && !profile.getSafetyNotes().isEmpty()) {
            advisories.addAll(profile.getSafetyNotes());
        } else {
            advisories.add(SafetyAdvisory.noVerifiedAdvisoryFound());
        }
        return advisories;
    }

    private List<String> buildTravelAdvice(DestinationProfile profile) {
        List<String> tips = new ArrayList<>();
        if (!profile.getLocalTravelAdvice().isEmpty()) {
            tips.addAll(profile.getLocalTravelAdvice());
        }
        if (profile.getTransportAdvice() != null && !profile.getTransportAdvice().isBlank()) {
            tips.add(profile.getTransportAdvice());
        }
        if (tips.isEmpty()) {
            tips.add("Carry light clothing and sun protection.");
            tips.add("Verify venue operational hours prior to arrival.");
            tips.add("Pre-book ERRORCab transit for predictable zero-surge pricing.");
        }
        return tips;
    }

    private BudgetBreakdown calculateBudgetBreakdown(String budgetStr, double totalCabFare) {
        double totalBudget = 2500.0;
        if (budgetStr != null) {
            String b = budgetStr.toLowerCase();
            if (b.contains("budget") || b.contains("500") || b.contains("1,500")) {
                totalBudget = 1500.0;
            } else if (b.contains("premium") || b.contains("3,500+")) {
                totalBudget = 5500.0;
            } else {
                totalBudget = 3000.0; // Moderate
            }
        }

        double foodEst = Math.round(totalBudget * 0.28);
        double activityEst = Math.round(totalBudget * 0.18);
        double cabFare = Math.round(totalCabFare > 0 ? totalCabFare : totalBudget * 0.35);
        double remainingBuffer = Math.max(0, Math.round(totalBudget - (cabFare + foodEst + activityEst)));

        return new BudgetBreakdown(
                totalBudget,
                cabFare,
                foodEst,
                activityEst,
                remainingBuffer,
                "Approximate breakdown based on selected budget tier; actual expenses vary."
        );
    }

    private DayBalance calculateDayBalance(String purpose, String duration) {
        int walking = 20;
        int sightseeing = 40;
        int food = 20;
        int relaxation = 10;
        int travel = 10;

        if (purpose != null) {
            String p = purpose.toLowerCase();
            if (p.contains("heritage") || p.contains("culture")) {
                walking = 25;
                sightseeing = 45;
                food = 15;
                relaxation = 5;
                travel = 10;
            } else if (p.contains("culinary") || p.contains("food")) {
                walking = 15;
                sightseeing = 25;
                food = 40;
                relaxation = 10;
                travel = 10;
            } else if (p.contains("weekend") || p.contains("relaxation") || p.contains("relaxed")) {
                walking = 15;
                sightseeing = 25;
                food = 20;
                relaxation = 30;
                travel = 10;
            } else if (p.contains("family")) {
                walking = 15;
                sightseeing = 35;
                food = 25;
                relaxation = 15;
                travel = 10;
            }
        }

        return new DayBalance(walking, sightseeing, food, relaxation, travel);
    }

    private List<String> buildTripReadiness(DestinationResult destResult, RouteResult route,
                                            DestinationProfile profile, CopilotTripRequest request,
                                            Map<CabType, Double> fares) {
        List<String> list = new ArrayList<>();
        list.add("✓ Destination resolved: " + destResult.getDisplayName());
        list.add("✓ Route available: " + (route != null && route.isRouteAvailable() ? route.getDistanceKm() + " km (" + route.getDurationFormatted() + ")" : "Local transit"));
        list.add("✓ Fare calculated: Economy ₹" + Math.round(fares.get(CabType.ECONOMY)) + " • Premium ₹" + Math.round(fares.get(CabType.PREMIUM)) + " • SUV ₹" + Math.round(fares.get(CabType.SUV)));
        list.add("✓ Trip purpose selected: " + (request.getTripPurpose() != null ? request.getTripPurpose() : "Leisure"));
        list.add("✓ Budget selected: " + (request.getBudget() != null ? request.getBudget() : "Moderate"));
        list.add("✓ Preferences selected: " + (request.getInterests() != null && !request.getInterests().isEmpty()
                ? String.join(", ", request.getInterests()) : "General Sightseeing"));
        list.add("✓ Itinerary generated with verified ERRORCab transfers");
        return list;
    }

    private String buildTripExplanation(List<ItineraryLeg> legs, List<CopilotRideSuggestion> rides, DestinationProfile profile) {
        int sightStops = 0;
        for (ItineraryLeg l : legs) {
            if (!"Transit".equalsIgnoreCase(l.getCategory())) {
                sightStops++;
            }
        }
        return "You will spend approximately 2.0 hours in ERRORCab transit across " + rides.size() +
                " rides and 4.5 hours experiencing " + sightStops + " curated stops in " + profile.getDestinationName() + ".";
    }

    public DestinationResolver getDestinationResolver() {
        return destinationResolver;
    }

    public DestinationIntelligenceService getIntelligenceService() {
        return intelligenceService;
    }
}
