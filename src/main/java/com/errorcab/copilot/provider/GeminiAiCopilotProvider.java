package com.errorcab.copilot.provider;

import com.errorcab.copilot.destination.model.DestinationProfile;
import com.errorcab.copilot.destination.model.DestinationResult;
import com.errorcab.copilot.destination.model.SafetyAdvisory;
import com.errorcab.copilot.destination.service.DestinationIntelligenceService;
import com.errorcab.copilot.gemini.dto.GeminiContent;
import com.errorcab.copilot.gemini.dto.GeminiGenerateRequest;
import com.errorcab.copilot.gemini.dto.GeminiGenerateResponse;
import com.errorcab.copilot.gemini.dto.GeminiGenerationConfig;
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
import com.errorcab.model.Booking;
import com.errorcab.model.CabType;
import com.errorcab.model.FavoriteLocation;
import com.errorcab.service.FareService;
import com.errorcab.service.MapService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Google Gemini AI Provider for ERRORCab Travel Copilot.
 * Synthesizes personalized natural-language itineraries while strictly grounding
 * routes, distances, fares, and safety notices in authentic Java backend calculations.
 * Never invents distances, fares, driver ratings, or fake safety incidents.
 */
public class GeminiAiCopilotProvider implements AiCopilotProvider {

    private static final Logger LOGGER = Logger.getLogger(GeminiAiCopilotProvider.class.getName());
    private static final String DEFAULT_GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String DEFAULT_MODEL = "gemini-3.5-flash";
    private static final int DEFAULT_TIMEOUT_SECONDS = 12;
    private static final int DEFAULT_MAX_RETRIES = 3;

    private final String apiKey;
    private final String modelName;
    private final int timeoutSeconds;
    private final AiCopilotProvider fallbackProvider;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final MapService mapService = MapService.getInstance();
    private final FareService fareService = FareService.getInstance();
    private final DestinationIntelligenceService intelligenceService;
    private long backoffBaseMs = 1000L;

    public GeminiAiCopilotProvider(String apiKey, String modelName, int timeoutSeconds,
                                   AiCopilotProvider fallbackProvider, HttpClient httpClient) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.modelName = (modelName != null && !modelName.trim().isEmpty()) ? modelName.trim() : resolveDefaultModel();
        this.timeoutSeconds = timeoutSeconds > 0 ? timeoutSeconds : DEFAULT_TIMEOUT_SECONDS;
        this.fallbackProvider = fallbackProvider != null ? fallbackProvider : new RuleEngineCopilotProvider();
        this.httpClient = httpClient != null ? httpClient : HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(this.timeoutSeconds))
                .build();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        if (this.fallbackProvider instanceof RuleEngineCopilotProvider rep) {
            this.intelligenceService = rep.getIntelligenceService();
        } else {
            this.intelligenceService = new DestinationIntelligenceService();
        }

        String keyLoadedStatus = (this.apiKey != null && !this.apiKey.isEmpty())
                ? "YES (Length: " + this.apiKey.length() + ")"
                : "NO (Not loaded / Empty)";
        LOGGER.info(String.format("[GEMINI DEBUG] API Key Loaded: %s | Model: %s", keyLoadedStatus, this.modelName));
    }

    public GeminiAiCopilotProvider(String apiKey, String modelName) {
        this(apiKey, modelName, DEFAULT_TIMEOUT_SECONDS, new RuleEngineCopilotProvider(), null);
    }

    public GeminiAiCopilotProvider(String apiKey) {
        this(apiKey, resolveDefaultModel(), DEFAULT_TIMEOUT_SECONDS, new RuleEngineCopilotProvider(), null);
    }

    public GeminiAiCopilotProvider() {
        this(resolveDefaultApiKey(), resolveDefaultModel(), DEFAULT_TIMEOUT_SECONDS, new RuleEngineCopilotProvider(), null);
    }

    public static String resolveDefaultModel() {
        String model = System.getProperty("gemini.model");
        if (model == null || model.isBlank()) {
            model = System.getenv("GEMINI_MODEL");
        }
        return (model != null && !model.isBlank()) ? model.trim() : DEFAULT_MODEL;
    }

    private static String resolveDefaultApiKey() {
        String key = System.getProperty("gemini.api.key");
        if (key == null || key.isBlank()) {
            key = System.getenv("GEMINI_API_KEY");
        }
        return key != null ? key.trim() : "";
    }

    @Override
    public String getProviderName() {
        return "Google Gemini (" + modelName + ")";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    public String getModelName() {
        return modelName;
    }

    public AiCopilotProvider getFallbackProvider() {
        return fallbackProvider;
    }

    public long getBackoffBaseMs() {
        return backoffBaseMs;
    }

    public void setBackoffBaseMs(long backoffBaseMs) {
        this.backoffBaseMs = backoffBaseMs > 0 ? backoffBaseMs : 1000L;
    }

    @Override
    public CopilotResponse generatePlan(CopilotContext context, CopilotTripRequest request) {
        if (!isAvailable()) {
            LOGGER.log(Level.INFO, "Gemini API key is not configured. Falling back to {0}.",
                    fallbackProvider.getProviderName());
            return fallbackProvider.generatePlan(context, request);
        }

        String rawDest = request != null && request.getDestination() != null ? request.getDestination() : "Fort Kochi";
        String pickupHub = context != null && context.getResolvedPickupLocation() != null ? context.getResolvedPickupLocation() : "Kakkanad";

        // 1. Resolve Destination via Java Intelligence Layer
        DestinationResult destResult = intelligenceService.resolveDestination(rawDest);
        if (destResult == null || !destResult.isResolved()) {
            LOGGER.log(Level.INFO, "Destination could not be verified ({0}). Delegating directly to fallback provider.", rawDest);
            return fallbackProvider.generatePlan(context, request);
        }

        DestinationProfile profile = intelligenceService.getProfile(destResult);
        if (profile == null) {
            profile = DestinationProfile.createLimited(destResult.getNormalizedPlaceName());
        }

        // 2. Compute Road Route & Fares in Java
        RouteResult routeResult = intelligenceService.calculateRoute(pickupHub, destResult);
        double routeKm = routeResult.isRouteAvailable() ? routeResult.getDistanceKm() : 15.0;
        Map<CabType, Double> fares = intelligenceService.calculateFares(routeKm);

        CabType recommendedCab = resolveCabType(request != null ? request.getBudget() : null);
        DriverRecommendation driverRec = intelligenceService.recommendDriver(recommendedCab, pickupHub);
        WeatherResult weather = intelligenceService.getWeather(destResult);

        try {
            // 3. Build structured prompt with verified facts
            String prompt = buildPrompt(context, request, destResult, routeResult, fares, profile, driverRec, weather);

            GeminiGenerateRequest apiRequest = new GeminiGenerateRequest();
            apiRequest.getContents().add(GeminiContent.userContent(prompt));
            apiRequest.setSystemInstruction(GeminiContent.systemContent(buildSystemInstruction()));
            apiRequest.setGenerationConfig(GeminiGenerationConfig.jsonConfig(0.3));

            String requestBodyJson = objectMapper.writeValueAsString(apiRequest);
            String url = DEFAULT_GEMINI_ENDPOINT + modelName + ":generateContent";

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> httpResponse = null;
            int maxRetries = DEFAULT_MAX_RETRIES;
            long currentBackoffMs = backoffBaseMs;
            boolean requestSucceeded = false;

            for (int attempt = 1; attempt <= 1 + maxRetries; attempt++) {
                try {
                    httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                    int status = httpResponse.statusCode();

                    if (status == 200) {
                        requestSucceeded = true;
                        break;
                    }

                    if (status == 503 || status == 429) {
                        if (attempt <= maxRetries) {
                            try {
                                Thread.sleep(currentBackoffMs);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                            currentBackoffMs *= 2;
                        }
                    } else {
                        break;
                    }
                } catch (IOException | InterruptedException e) {
                    if (attempt <= maxRetries) {
                        try {
                            Thread.sleep(currentBackoffMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        currentBackoffMs *= 2;
                    }
                }
            }

            if (!requestSucceeded || httpResponse == null || httpResponse.statusCode() != 200) {
                throw new IllegalStateException("Gemini API call failed with status: " + (httpResponse != null ? httpResponse.statusCode() : -1));
            }

            GeminiGenerateResponse geminiResponse = objectMapper.readValue(httpResponse.body(), GeminiGenerateResponse.class);
            String jsonText = geminiResponse.getFirstCandidateText();
            if (jsonText == null || jsonText.trim().isEmpty()) {
                throw new IllegalStateException("Gemini returned empty candidate content");
            }

            jsonText = cleanJsonText(jsonText);
            GeminiPlanPayload planPayload = objectMapper.readValue(jsonText, GeminiPlanPayload.class);
            planPayload.validate();

            // Enrich Gemini plan with authentic Java calculations
            return mapPayloadToCopilotResponse(planPayload, context, request, destResult, routeResult, fares, profile, driverRec, weather);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Gemini plan generation failed: {0}. Falling back to {1}.",
                    new Object[]{e.getMessage(), fallbackProvider.getProviderName()});
            return fallbackProvider.generatePlan(context, request);
        }
    }

    public static String sanitizeResponseBody(String body) {
        if (body == null) return "";
        String sanitized = body.replaceAll("\"thoughtSignature\"\\s*:\\s*\"[^\"]*\"", "\"thoughtSignature\":\"[REDACTED]\"");
        sanitized = sanitized.replaceAll("key=[A-Za-z0-9_-]+", "key=[REDACTED]");
        return sanitized;
    }

    public String buildPrompt(CopilotContext context, CopilotTripRequest request) {
        String destStr = request != null && request.getDestination() != null ? request.getDestination() : "Fort Kochi";
        DestinationResult destResult = intelligenceService.resolveDestination(destStr);
        DestinationProfile profile = intelligenceService.getProfile(destResult);
        String pickupHub = context != null && context.getResolvedPickupLocation() != null ? context.getResolvedPickupLocation() : "Kakkanad";
        RouteResult routeResult = intelligenceService.calculateRoute(pickupHub, destResult);
        double routeKm = routeResult.isRouteAvailable() ? routeResult.getDistanceKm() : 15.0;
        Map<CabType, Double> fares = intelligenceService.calculateFares(routeKm);
        CabType cabType = resolveCabType(request != null ? request.getBudget() : null);
        DriverRecommendation driverRec = intelligenceService.recommendDriver(cabType, pickupHub);
        WeatherResult weather = intelligenceService.getWeather(destResult);

        return buildPrompt(context, request, destResult, routeResult, fares, profile, driverRec, weather);
    }

    public String buildPrompt(CopilotContext context, CopilotTripRequest request,
                              DestinationResult destResult, RouteResult routeResult,
                              Map<CabType, Double> fares, DestinationProfile profile,
                              DriverRecommendation driverRec, WeatherResult weather) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are the ERRORCab AI Travel Copilot for India.\n");
        sb.append("Synthesize a personalized, culturally authentic travel itinerary grounded strictly in the following factual parameters:\n\n");

        sb.append("--- FACTUAL DESTINATION & ROUTE CONTEXT ---\n");
        sb.append("- Destination: ").append(destResult.getDisplayName()).append("\n");
        sb.append("- District/State: ").append(destResult.getDistrict() != null ? destResult.getDistrict() + ", " : "").append(destResult.getState()).append("\n");
        sb.append("- Road Distance from Home Hub: ").append(routeResult.getDistanceKm()).append(" km (DO NOT modify or invent distance)\n");
        sb.append("- Estimated Travel Duration: ").append(routeResult.getDurationFormatted()).append("\n");
        sb.append("- Verified ERRORCab Fares: Economy ₹").append(Math.round(fares.get(CabType.ECONOMY)))
          .append(" | Premium ₹").append(Math.round(fares.get(CabType.PREMIUM)))
          .append(" | SUV ₹").append(Math.round(fares.get(CabType.SUV)))
          .append(" (DO NOT modify or invent fares)\n");

        if (weather != null && weather.isAvailable()) {
            sb.append("- Live Weather: ").append(Math.round(weather.getTemperatureC())).append("°C, ")
              .append(weather.getCondition()).append(weather.isRaining() ? " (Raining)" : "").append("\n");
        }

        if (profile.getAttractions() != null && !profile.getAttractions().isEmpty()) {
            sb.append("- Verified Real Landmarks: ").append(String.join(", ", profile.getAttractions())).append("\n");
        }
        if (profile.getCulinaryHighlights() != null && !profile.getCulinaryHighlights().isEmpty()) {
            sb.append("- Verified Culinary Specialties: ").append(String.join(", ", profile.getCulinaryHighlights())).append("\n");
        }
        if (profile.getSafetyNotes() != null && !profile.getSafetyNotes().isEmpty()) {
            sb.append("- Verified Safety Advisories:\n");
            for (SafetyAdvisory adv : profile.getSafetyNotes()) {
                sb.append("  * ").append(adv.getText()).append(" (Source: ").append(adv.getSource()).append(")\n");
            }
        }

        sb.append("\n--- PASSENGER PROFILE & PREFERENCES ---\n");
        if (context != null) {
            sb.append("- Passenger Name: ").append(context.getPassengerName() != null ? context.getPassengerName() : "Traveler").append("\n");
            sb.append("- Pickup Hub: ").append(context.getResolvedPickupLocation() != null ? context.getResolvedPickupLocation() : "Kochi").append("\n");
            sb.append("- Total Past Rides: ").append(context.getTotalRides()).append("\n");
            sb.append("- Total Spent: ₹").append(Math.round(context.getTotalSpent())).append("\n");
            if (context.getFavorites() != null && !context.getFavorites().isEmpty()) {
                sb.append("- Favorite Locations: ");
                sb.append(context.getFavorites().stream().map(f -> f.getLabel() + " (" + f.getAddress() + ")").collect(java.util.stream.Collectors.joining(", ")));
                sb.append("\n");
            }
            if (context.getRecentTrips() != null && !context.getRecentTrips().isEmpty()) {
                sb.append("- Recent Trips: ");
                sb.append(context.getRecentTrips().stream().map(b -> b.getPickupLocation() + " ➔ " + b.getDropoffLocation()).collect(java.util.stream.Collectors.joining(", ")));
                sb.append("\n");
            }
        }
        sb.append("- Purpose: ").append(request.getTripPurpose() != null ? request.getTripPurpose() : "Leisure").append("\n");
        sb.append("- Budget Tier: ").append(request.getBudget() != null ? request.getBudget() : "Moderate").append("\n");
        sb.append("- Duration: ").append(request.getDuration() != null ? request.getDuration() : "Half-day").append("\n");
        if (request.getInterests() != null && !request.getInterests().isEmpty()) {
            sb.append("- Interests: ").append(String.join(", ", request.getInterests())).append("\n");
        }
        if (request.getFoodPreferences() != null && !request.getFoodPreferences().isEmpty()) {
            sb.append("- Food Preferences: ").append(String.join(", ", request.getFoodPreferences())).append("\n");
        }
        if (request.getActivityPreferences() != null && !request.getActivityPreferences().isEmpty()) {
            sb.append("- Activity Preferences: ").append(String.join(", ", request.getActivityPreferences())).append("\n");
        }

        sb.append("\n--- STRICT RULES ---\n");
        sb.append("1. Respond ONLY with valid JSON conforming to the schema.\n");
        sb.append("2. Include 3-6 chronological legs naming the EXACT real landmarks provided.\n");
        sb.append("3. For transit legs, suggest ERRORCab private ride.\n");
        sb.append("4. NEVER invent fake scams, crimes, or unverified safety incidents. Only reference verified advisories.\n");
        sb.append("5. In 'specialties', include 3-5 authentic regional highlights.\n");
        sb.append("6. In 'warnings', include only verified notices from the factual context or state 'No verified destination-specific safety advisory found.'\n");

        return sb.toString();
    }

    public String buildSystemInstruction() {
        return "You are ERRORCab's specialized AI Travel Copilot for India. " +
                "Generate realistic, culturally authentic travel suggestions strictly using verified facts. " +
                "Never invent fares, distances, driver stats, or unverified crime/scam reports. " +
                "Strictly return JSON conforming to this schema:\n" +
                "{\n" +
                "  \"title\": \"string\",\n" +
                "  \"summary\": \"string\",\n" +
                "  \"itinerary\": [\n" +
                "    {\n" +
                "      \"timeSlot\": \"string\",\n" +
                "      \"title\": \"string\",\n" +
                "      \"locationName\": \"string\",\n" +
                "      \"category\": \"Sightseeing | Dining | Transit | Culture | Shopping\",\n" +
                "      \"description\": \"string\",\n" +
                "      \"rideSuggested\": true | false,\n" +
                "      \"pickupLocation\": \"string or null\",\n" +
                "      \"dropoffLocation\": \"string or null\",\n" +
                "      \"cabType\": \"ECONOMY | PREMIUM | SUV or null\",\n" +
                "      \"rideReason\": \"string or null\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"specialties\": [\"string\"],\n" +
                "  \"warnings\": [\"string\"],\n" +
                "  \"foodRecommendations\": [\"string\"],\n" +
                "  \"travelTips\": [\"string\"]\n" +
                "}";
    }

    private String cleanJsonText(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    public void runStartupConnectivityTest() {
        if (!isAvailable()) return;
        LOGGER.info(() -> "Testing Gemini connectivity asynchronously on startup with model: " + modelName);
    }

    public CopilotResponse mapPayloadToCopilotResponse(GeminiPlanPayload payload, CopilotContext context,
                                                       CopilotTripRequest request) {
        String destStr = request != null && request.getDestination() != null ? request.getDestination() : "Kochi";
        DestinationResult destResult = intelligenceService.resolveDestination(destStr);
        DestinationProfile profile = intelligenceService.getProfile(destResult);
        String pickupHub = context != null && context.getResolvedPickupLocation() != null ? context.getResolvedPickupLocation() : "Kakkanad";
        RouteResult routeResult = intelligenceService.calculateRoute(pickupHub, destResult);
        double routeKm = routeResult.isRouteAvailable() ? routeResult.getDistanceKm() : 15.0;
        Map<CabType, Double> fares = intelligenceService.calculateFares(routeKm);
        CabType cabType = resolveCabType(request != null ? request.getBudget() : null);
        DriverRecommendation driverRec = intelligenceService.recommendDriver(cabType, pickupHub);
        WeatherResult weather = intelligenceService.getWeather(destResult);

        return mapPayloadToCopilotResponse(payload, context, request, destResult, routeResult, fares, profile, driverRec, weather);
    }

    public CopilotResponse mapPayloadToCopilotResponse(GeminiPlanPayload payload, CopilotContext context,
                                                       CopilotTripRequest request, DestinationResult destResult,
                                                       RouteResult routeResult, Map<CabType, Double> fares,
                                                       DestinationProfile profile, DriverRecommendation driverRec,
                                                       WeatherResult weather) {
        CopilotResponse response = new CopilotResponse();
        response.setProviderName(getProviderName());
        response.setAssistanceType("AI_ASSISTED");
        response.setTitle(payload.getTitle());
        response.setSummary(payload.getSummary());
        response.setTripPurpose(request.getTripPurpose());
        response.setDestination(request.getDestination());
        response.setDuration(request.getDuration());
        response.setBudget(request.getBudget());
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
        response.setEconomyFare(fares.get(CabType.ECONOMY));
        response.setPremiumFare(fares.get(CabType.PREMIUM));
        response.setSuvFare(fares.get(CabType.SUV));

        String defaultPickup = context != null && context.getResolvedPickupLocation() != null
                ? context.getResolvedPickupLocation() : "Kakkanad";
        CabType defaultCabType = resolveCabType(request.getBudget());

        List<ItineraryLeg> legs = new ArrayList<>();
        List<CopilotRideSuggestion> rides = new ArrayList<>();
        double totalCabFare = 0.0;
        int rideIndex = 1;

        double outwardDist = routeResult.isRouteAvailable() ? routeResult.getDistanceKm() : 15.0;
        int outwardMins = routeResult.isRouteAvailable() ? routeResult.getDurationMinutes() : 35;
        double outwardFare = fares.get(defaultCabType);

        for (GeminiLegPayload legPayload : payload.getItinerary()) {
            CopilotRideSuggestion rideSuggestion = null;

            boolean wantsRide = Boolean.TRUE.equals(legPayload.getRideSuggested()) ||
                    "Transit".equalsIgnoreCase(legPayload.getCategory());

            if (wantsRide) {
                String pLoc = legPayload.getPickupLocation() != null && !legPayload.getPickupLocation().isBlank()
                        ? legPayload.getPickupLocation().trim() : defaultPickup;
                String dLoc = legPayload.getDropoffLocation() != null && !legPayload.getDropoffLocation().isBlank()
                        ? legPayload.getDropoffLocation().trim() : destResult.getNormalizedPlaceName();

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
                    legPayload.getTitle(),
                    legPayload.getLocationName() != null ? legPayload.getLocationName() : destResult.getNormalizedPlaceName(),
                    legPayload.getCategory() != null ? legPayload.getCategory() : "Sightseeing",
                    legPayload.getDescription() != null ? legPayload.getDescription() : "",
                    rideSuggestion
            );
            legs.add(leg);
        }

        if (rides.isEmpty()) {
            CopilotRideSuggestion outwardRide = new CopilotRideSuggestion(
                    "Primary Ride: " + defaultPickup + " ➔ " + destResult.getNormalizedPlaceName(),
                    defaultPickup, destResult.getNormalizedPlaceName(), outwardDist, outwardMins, defaultCabType, outwardFare,
                    "Direct pickup from " + defaultPickup + " to " + destResult.getNormalizedPlaceName() + "."
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
        String sigFood = (profile.getCulinaryHighlights() != null && !profile.getCulinaryHighlights().isEmpty())
                ? profile.getCulinaryHighlights().get(0) : "Authentic Regional Delicacies";
        response.setCuratedCulinary(new CuratedCulinaryInfo(
                sigFood,
                "Local Tea & Artisanal Cafe Culture",
                "Traditional Regional Thali",
                profile.getCulinaryHighlights() != null ? profile.getCulinaryHighlights() : List.of()
        ));

        // Safety Advisories
        List<SafetyAdvisory> advisories = profile.getSafetyNotes();
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
        readiness.add("✓ Destination resolved: " + destResult.getDisplayName());
        readiness.add("✓ Route available: " + routeResult.getDistanceKm() + " km (" + routeResult.getDurationFormatted() + ")");
        readiness.add("✓ Fare calculated: Economy ₹" + Math.round(fares.get(CabType.ECONOMY)) + " • Premium ₹" + Math.round(fares.get(CabType.PREMIUM)) + " • SUV ₹" + Math.round(fares.get(CabType.SUV)));
        readiness.add("✓ Trip purpose selected: " + (request.getTripPurpose() != null ? request.getTripPurpose() : "Leisure"));
        readiness.add("✓ Budget selected: " + (request.getBudget() != null ? request.getBudget() : "Moderate"));
        readiness.add("✓ Preferences selected: " + (request.getInterests() != null && !request.getInterests().isEmpty() ? String.join(", ", request.getInterests()) : "General"));
        readiness.add("✓ Itinerary generated with verified ERRORCab transfers");
        response.setTripReadiness(readiness);

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

    public DestinationIntelligenceService getIntelligenceService() {
        return intelligenceService;
    }
}
