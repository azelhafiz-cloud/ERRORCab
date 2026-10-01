package com.errorcab.copilot.provider;

import com.errorcab.copilot.gemini.dto.GeminiContent;
import com.errorcab.copilot.gemini.dto.GeminiGenerateRequest;
import com.errorcab.copilot.gemini.dto.GeminiGenerateResponse;
import com.errorcab.copilot.gemini.dto.GeminiGenerationConfig;
import com.errorcab.copilot.gemini.dto.GeminiLegPayload;
import com.errorcab.copilot.gemini.dto.GeminiPlanPayload;
import com.errorcab.copilot.model.CopilotContext;
import com.errorcab.copilot.model.CopilotResponse;
import com.errorcab.copilot.model.CopilotRideSuggestion;
import com.errorcab.copilot.model.CopilotTripRequest;
import com.errorcab.copilot.model.ItineraryLeg;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Google Gemini AI Provider for ERRORCab Travel Copilot.
 * Synthesizes personalized travel itineraries and ERRORCab ride suggestions
 * using Gemini generative models.
 * Automatically falls back to RuleEngineCopilotProvider if the API key is missing,
 * network is unreachable, or response validation fails.
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

        // Temporary Debug Logging for Gemini API Investigation
        String keyLoadedStatus = (this.apiKey != null && !this.apiKey.isEmpty())
                ? "YES (First 4 chars: " + (this.apiKey.length() >= 4 ? this.apiKey.substring(0, 4) : this.apiKey) + "..., Length: " + this.apiKey.length() + ")"
                : "NO (Not loaded / Empty)";
        String endpointUrl = DEFAULT_GEMINI_ENDPOINT + this.modelName + ":generateContent";
        String authMethod = "Header (x-goog-api-key) [NOT Authorization: Bearer]";
        LOGGER.info(String.format(
                "[GEMINI DEBUG] API Key Loaded: %s | Endpoint: %s | Model: %s | Auth Method: %s",
                keyLoadedStatus, endpointUrl, this.modelName, authMethod));
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
        // Temporary Debug Logging for Gemini API Investigation
        String keyLoadedStatus = (this.apiKey != null && !this.apiKey.isEmpty())
                ? "YES (First 4 chars: " + (this.apiKey.length() >= 4 ? this.apiKey.substring(0, 4) : this.apiKey) + "..., Length: " + this.apiKey.length() + ")"
                : "NO (Not loaded / Empty)";
        String endpointUrl = DEFAULT_GEMINI_ENDPOINT + this.modelName + ":generateContent";
        String authMethod = "Header (x-goog-api-key) [NOT Authorization: Bearer]";
        LOGGER.info(String.format(
                "[GEMINI DEBUG EXECUTION] API Key Loaded: %s | Endpoint: %s | Model: %s | Auth Method: %s",
                keyLoadedStatus, endpointUrl, this.modelName, authMethod));

        // 1. Check if Gemini API key is configured
        if (!isAvailable()) {
            LOGGER.log(Level.INFO, "Gemini API key is not configured. Falling back to {0}.",
                    fallbackProvider.getProviderName());
            return fallbackProvider.generatePlan(context, request);
        }

        try {
            // 2. Build structured prompt with passenger context and travel preferences
            String prompt = buildPrompt(context, request);

            // 3. Construct Gemini API request payload requiring JSON output
            GeminiGenerateRequest apiRequest = new GeminiGenerateRequest();
            apiRequest.getContents().add(GeminiContent.userContent(prompt));
            apiRequest.setSystemInstruction(GeminiContent.systemContent(buildSystemInstruction()));
            apiRequest.setGenerationConfig(GeminiGenerationConfig.jsonConfig(0.4));

            String requestBodyJson = objectMapper.writeValueAsString(apiRequest);

            String url = DEFAULT_GEMINI_ENDPOINT + modelName + ":generateContent";
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            // 4. Execute HTTP Call with retry 3 times and exponential backoff
            HttpResponse<String> httpResponse = null;
            int maxRetries = DEFAULT_MAX_RETRIES;
            long currentBackoffMs = backoffBaseMs;
            boolean requestSucceeded = false;

            for (int attempt = 1; attempt <= 1 + maxRetries; attempt++) {
                try {
                    httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                    int status = httpResponse.statusCode();

                    LOGGER.info(String.format("[GEMINI HTTP] Attempt %d/%d | Model: '%s' | Response Status: %d",
                            attempt, 1 + maxRetries, modelName, status));

                    if (status == 200) {
                        requestSucceeded = true;
                        break;
                    }

                    // Handle transient capacity errors (503 Service Unavailable / 429 Rate Limit)
                    if (status == 503 || status == 429) {
                        if (attempt <= maxRetries) {
                            LOGGER.warning(String.format(
                                    "[GEMINI RETRY] Attempt %d/%d: Model '%s' returned HTTP %d (high demand / capacity). Retrying in %d ms (exponential backoff)...",
                                    attempt, 1 + maxRetries, modelName, status, currentBackoffMs));
                            try {
                                Thread.sleep(currentBackoffMs);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                            currentBackoffMs *= 2; // Exponential backoff: 1s, 2s, 4s
                        } else {
                            LOGGER.warning(String.format(
                                    "[GEMINI RETRY EXHAUSTED] HTTP %d persisted after %d retries for model '%s'. Falling back to %s.",
                                    status, maxRetries, modelName, fallbackProvider.getProviderName()));
                        }
                    } else {
                        // Non-retryable error (e.g. 400, 401, 404, etc.)
                        LOGGER.warning(String.format(
                                "[GEMINI ERROR] Attempt %d/%d: Model '%s' returned non-retryable HTTP %d. Body: %s",
                                attempt, 1 + maxRetries, modelName, status, sanitizeResponseBody(httpResponse.body())));
                        break;
                    }
                } catch (IOException | InterruptedException e) {
                    LOGGER.warning(String.format("[GEMINI NETWORK ERROR] Attempt %d/%d for model '%s' failed: %s",
                            attempt, 1 + maxRetries, modelName, e.getMessage()));
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
                int finalStatus = httpResponse != null ? httpResponse.statusCode() : -1;
                String finalBody = httpResponse != null ? sanitizeResponseBody(httpResponse.body()) : "No HTTP response received";
                if (finalStatus == 503) {
                    LOGGER.warning(String.format(
                            "[GEMINI 503 FALLBACK] High demand persisted for model '%s' after %d retries. Gracefully falling back to %s.",
                            modelName, maxRetries, fallbackProvider.getProviderName()));
                }
                throw new IllegalStateException("Gemini API returned HTTP status " + finalStatus + ": " + finalBody);
            }

            // 5. Parse Gemini response
            GeminiGenerateResponse geminiResponse = objectMapper.readValue(httpResponse.body(), GeminiGenerateResponse.class);
            String jsonText = geminiResponse.getFirstCandidateText();
            if (jsonText == null || jsonText.trim().isEmpty()) {
                throw new IllegalStateException("Gemini returned empty candidate content");
            }

            // Clean markdown code blocks if returned (e.g. ```json ... ```)
            jsonText = cleanJsonText(jsonText);

            // 6. Parse and validate structured plan payload
            GeminiPlanPayload planPayload = objectMapper.readValue(jsonText, GeminiPlanPayload.class);
            planPayload.validate();

            // 7. Enrich with ERRORCab fares, distances, and 1-click booking URLs
            return mapPayloadToCopilotResponse(planPayload, context, request);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Gemini plan generation failed: {0}. Falling back to {1}.",
                    new Object[]{e.getMessage(), fallbackProvider.getProviderName()});
            CopilotResponse fallbackRes = fallbackProvider.generatePlan(context, request);
            return fallbackRes;
        }
    }

    /**
     * Builds the structured user prompt embedding all passenger context and requested preferences.
     */
    public String buildPrompt(CopilotContext context, CopilotTripRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are the ERRORCab AI Travel Copilot for Kerala, India.\n");
        sb.append("Create a personalized, high quality travel itinerary and ERRORCab ride plan based on the following passenger context and preferences:\n\n");

        sb.append("--- PASSENGER PROFILE & CONTEXT ---\n");
        if (context != null) {
            sb.append("- Passenger Name: ").append(context.getPassengerName() != null ? context.getPassengerName() : "Valued Passenger").append("\n");
            sb.append("- Default Home / Pickup Hub: ").append(context.getResolvedPickupLocation() != null ? context.getResolvedPickupLocation() : "Kochi").append("\n");
            sb.append("- Total Past Rides on ERRORCab: ").append(context.getTotalRides()).append("\n");
            sb.append("- Total Spent on ERRORCab: ₹").append(context.getTotalSpent()).append("\n");

            if (context.getFavorites() != null && !context.getFavorites().isEmpty()) {
                sb.append("- Saved Favorite Locations:\n");
                for (FavoriteLocation fav : context.getFavorites()) {
                    sb.append("  * ").append(fav.getLabel()).append(": ").append(fav.getLocationName())
                      .append(" (").append(fav.getAddress()).append(")\n");
                }
            }

            if (context.getRecentTrips() != null && !context.getRecentTrips().isEmpty()) {
                sb.append("- Recent Trip History:\n");
                int limit = Math.min(context.getRecentTrips().size(), 3);
                for (int i = 0; i < limit; i++) {
                    Booking b = context.getRecentTrips().get(i);
                    sb.append("  * ").append(b.getPickupLocation()).append(" ➔ ").append(b.getDestinationLocation())
                      .append(" (").append(b.getCabType()).append(", ").append(b.getStatus()).append(")\n");
                }
            }
        }

        sb.append("\n--- TRAVEL PREFERENCES ---\n");
        sb.append("- Primary Trip Purpose: ").append(request.getTripPurpose() != null ? request.getTripPurpose() : "Leisure & Tourism").append("\n");
        sb.append("- Destination / Hub: ").append(request.getDestination() != null ? request.getDestination() : "Fort Kochi").append("\n");
        sb.append("- Duration: ").append(request.getDuration() != null ? request.getDuration() : "Half-day (4-5 hrs)").append("\n");
        sb.append("- Budget Tier: ").append(request.getBudget() != null ? request.getBudget() : "Moderate (₹1,500 - ₹3,500)").append("\n");

        if (request.getInterests() != null && !request.getInterests().isEmpty()) {
            sb.append("- Selected Interests: ").append(String.join(", ", request.getInterests())).append("\n");
        }
        if (request.getFoodPreferences() != null && !request.getFoodPreferences().isEmpty()) {
            sb.append("- Food & Dining Preferences: ").append(String.join(", ", request.getFoodPreferences())).append("\n");
        }
        if (request.getActivityPreferences() != null && !request.getActivityPreferences().isEmpty()) {
            sb.append("- Activity Preferences: ").append(String.join(", ", request.getActivityPreferences())).append("\n");
        }

        sb.append("\n--- INSTRUCTIONS ---\n");
        sb.append("1. Respond ONLY with a valid JSON object matching the required schema.\n");
        sb.append("2. Include 3-6 chronological itinerary legs with exact scheduled timing (e.g. '10:30 AM - 01:00 PM'). Every leg MUST specify the EXACT, SPECIFIC landmark, beach, heritage site, or establishment being visited in 'locationName' and 'title' (e.g. 'Fort Aguada & Lighthouse', 'Baga Beach Promenade', 'Eravikulam National Park (Rajamalai)', 'KDHP Tea Museum'). NEVER use generic words like 'Destination Central' or 'Core Sights'.\n");
        sb.append("3. For transit legs (e.g. between home hub and destination or between distant stops), set 'rideSuggested' to true and specify 'pickupLocation' and 'dropoffLocation'.\n");
        sb.append("4. In 'specialties', list 3-5 iconic regional specialities of the destination (unique cultural traits, famous local handicrafts, signature dishes, or historic landmarks).\n");
        sb.append("5. In 'warnings', list 3-5 critical place warnings and safety advisories (e.g. rough sea/high tide warnings, ghat road fog, dress codes, entry permits, closing hours, peak rush hours).\n");
        sb.append("6. Recommend authentic local culinary spots matching the food preferences.\n");
        sb.append("7. Include practical travel, timing, and local tips.\n");

        return sb.toString();
    }

    public String buildSystemInstruction() {
        return "You are ERRORCab's specialized AI Travel Copilot for India and Kerala. " +
                "Always generate realistic, culturally rich, safe travel suggestions. " +
                "Every itinerary leg must name the exact real-world visiting place/landmark (e.g. 'Fort Aguada', 'Eravikulam National Park') and specific time window. " +
                "You must strictly return JSON conforming to this schema:\n" +
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

    /**
     * Enriches the parsed Gemini payload with authentic ERRORCab pricing, distances,
     * and 1-click booking URLs.
     */
    public CopilotResponse mapPayloadToCopilotResponse(GeminiPlanPayload payload, CopilotContext context,
                                                       CopilotTripRequest request) {
        CopilotResponse response = new CopilotResponse();
        response.setProviderName(getProviderName());
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

        String defaultPickup = context != null && context.getResolvedPickupLocation() != null
                ? context.getResolvedPickupLocation() : "Kakkanad";
        CabType defaultCabType = resolveCabType(request.getBudget());

        List<ItineraryLeg> legs = new ArrayList<>();
        List<CopilotRideSuggestion> rides = new ArrayList<>();
        double totalCabFare = 0.0;
        int rideIndex = 1;

        for (GeminiLegPayload legPayload : payload.getItinerary()) {
            CopilotRideSuggestion rideSuggestion = null;

            boolean wantsRide = Boolean.TRUE.equals(legPayload.getRideSuggested()) ||
                    "Transit".equalsIgnoreCase(legPayload.getCategory()) ||
                    (legPayload.getPickupLocation() != null && legPayload.getDropoffLocation() != null &&
                            !legPayload.getPickupLocation().equalsIgnoreCase(legPayload.getDropoffLocation()));

            if (wantsRide) {
                String pLoc = legPayload.getPickupLocation() != null && !legPayload.getPickupLocation().isBlank()
                        ? legPayload.getPickupLocation().trim() : defaultPickup;
                String dLoc = legPayload.getDropoffLocation() != null && !legPayload.getDropoffLocation().isBlank()
                        ? legPayload.getDropoffLocation().trim() : (request.getDestination() != null ? request.getDestination() : "Fort Kochi");

                CabType cabType = defaultCabType;
                if (legPayload.getCabType() != null) {
                    try {
                        cabType = CabType.valueOf(legPayload.getCabType().toUpperCase().trim());
                    } catch (Exception ignored) {}
                }

                double distKm = mapService.getDistanceKm(pLoc, dLoc);
                int mins = mapService.getEstimatedMinutes(distKm);
                double fare = fareService.calculateFare(cabType, distKm);

                String reason = legPayload.getRideReason() != null ? legPayload.getRideReason() :
                        "Direct ERRORCab transit with zero surge pricing.";

                rideSuggestion = new CopilotRideSuggestion(
                        "Ride " + rideIndex + ": " + pLoc + " ➔ " + dLoc,
                        pLoc, dLoc, distKm, mins, cabType, fare, reason
                );

                rides.add(rideSuggestion);
                totalCabFare += fare;
                rideIndex++;
            }

            ItineraryLeg leg = new ItineraryLeg(
                    legPayload.getTimeSlot() != null ? legPayload.getTimeSlot() : "Flexible Time",
                    legPayload.getTitle(),
                    legPayload.getLocationName() != null ? legPayload.getLocationName() : request.getDestination(),
                    legPayload.getCategory() != null ? legPayload.getCategory() : "Sightseeing",
                    legPayload.getDescription() != null ? legPayload.getDescription() : "",
                    rideSuggestion
            );
            legs.add(leg);
        }

        // If Gemini didn't specify any transit legs, ensure at least the primary outward ride is provided
        if (rides.isEmpty()) {
            String dest = request.getDestination() != null ? request.getDestination() : "Fort Kochi";
            double distKm = mapService.getDistanceKm(defaultPickup, dest);
            int mins = mapService.getEstimatedMinutes(distKm);
            double fare = fareService.calculateFare(defaultCabType, distKm);

            CopilotRideSuggestion outwardRide = new CopilotRideSuggestion(
                    "Primary Ride: " + defaultPickup + " ➔ " + dest,
                    defaultPickup, dest, distKm, mins, defaultCabType, fare,
                    "Direct pickup from " + defaultPickup + " to " + dest + "."
            );
            rides.add(outwardRide);
            totalCabFare += fare;

            if (!legs.isEmpty()) {
                legs.get(0).setRideSuggestion(outwardRide);
            }
        }

        response.setItinerary(legs);
        response.setRecommendedRides(rides);
        response.setEstimatedTotalCabFare(Math.round(totalCabFare));

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

    /**
     * Temporary startup connectivity test: sends a minimal request ("Hello")
     * and logs the HTTP status code and response body.
     */
    public boolean runStartupConnectivityTest() {
        if (!isAvailable()) {
            LOGGER.info("[GEMINI STARTUP CONNECTIVITY] Skipped: API key is not configured.");
            return false;
        }

        int maxRetries = DEFAULT_MAX_RETRIES;
        long currentBackoffMs = backoffBaseMs;

        for (int attempt = 1; attempt <= 1 + maxRetries; attempt++) {
            try {
                String testUrl = DEFAULT_GEMINI_ENDPOINT + modelName + ":generateContent";
                String testBody = "{\"contents\":[{\"parts\":[{\"text\":\"Hello\"}]}]}";
                HttpRequest testRequest = HttpRequest.newBuilder()
                        .uri(URI.create(testUrl))
                        .header("Content-Type", "application/json")
                        .header("x-goog-api-key", apiKey)
                        .timeout(Duration.ofSeconds(timeoutSeconds))
                        .POST(HttpRequest.BodyPublishers.ofString(testBody))
                        .build();

                LOGGER.info(String.format(
                        "[GEMINI STARTUP CONNECTIVITY] Attempt %d/%d | Sending minimal test request ('Hello') for model '%s'...",
                        attempt, 1 + maxRetries, modelName));
                HttpResponse<String> testResponse = httpClient.send(testRequest, HttpResponse.BodyHandlers.ofString());
                int status = testResponse.statusCode();

                LOGGER.info(String.format(
                        "[GEMINI STARTUP CONNECTIVITY] Attempt %d/%d | Model: '%s' | Response Status: %d",
                        attempt, 1 + maxRetries, modelName, status));
                LOGGER.info("[GEMINI STARTUP CONNECTIVITY] Response Body: " + sanitizeResponseBody(testResponse.body()));

                if (status == 200) {
                    return true;
                }

                if ((status == 503 || status == 429) && attempt <= maxRetries) {
                    LOGGER.warning(String.format(
                            "[GEMINI STARTUP CONNECTIVITY] Model '%s' returned HTTP %d (high demand / capacity). Retrying in %d ms (exponential backoff)...",
                            modelName, status, currentBackoffMs));
                    try {
                        Thread.sleep(currentBackoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    currentBackoffMs *= 2;
                } else {
                    break;
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, String.format(
                        "[GEMINI STARTUP CONNECTIVITY] Attempt %d/%d for model '%s' failed with exception: %s",
                        attempt, 1 + maxRetries, modelName, e.getMessage()), e);
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
        return false;
    }

    /**
     * Sanitizes response strings before logging, redacting internal reasoning tokens
     * (thoughtSignature) and any sensitive credentials or keys.
     */
    public static String sanitizeResponseBody(String body) {
        if (body == null || body.isBlank()) {
            return body;
        }
        // Redact thoughtSignature strings (can be long base64 tokens)
        String sanitized = body.replaceAll("(\"thoughtSignature\"\\s*:\\s*\")[^\"]+(\")", "$1[REDACTED]$2");
        // Redact any potential API key patterns or authorization tokens
        sanitized = sanitized.replaceAll("(?i)(key=)[A-Za-z0-9_-]{10,}", "$1[REDACTED]");
        sanitized = sanitized.replaceAll("(?i)(\"apiKey\"\\s*:\\s*\")[^\"]+(\")", "$1[REDACTED]$2");
        sanitized = sanitized.replaceAll("(?i)(AIza[0-9A-Za-z-_]{35})", "[REDACTED_API_KEY]");
        return sanitized;
    }
}
