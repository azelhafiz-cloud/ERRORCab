package com.errorcab.copilot.provider;

import com.errorcab.copilot.gemini.dto.GeminiPlanPayload;
import com.errorcab.copilot.model.CopilotContext;
import com.errorcab.copilot.model.CopilotResponse;
import com.errorcab.copilot.model.CopilotTripRequest;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * External AI provider utilizing OpenRouter API.
 * Accesses OpenRouter models (default router: openrouter/free).
 * Automatically and seamlessly falls back to RuleEngineCopilotProvider upon any failure.
 */
public class OpenRouterAiCopilotProvider implements AiCopilotProvider {

    private static final Logger LOGGER = Logger.getLogger(OpenRouterAiCopilotProvider.class.getName());
    private static final String DEFAULT_ENDPOINT = "https://openrouter.ai/api/v1/chat/completions";
    private static final String DEFAULT_MODEL = "openrouter/free";
    private static final int DEFAULT_TIMEOUT_SECONDS = 15;

    private final String apiKey;
    private final String modelName;
    private final int timeoutSeconds;
    private final AiCopilotProvider fallbackProvider;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final CopilotPlanMapper planMapper;

    public OpenRouterAiCopilotProvider(String apiKey, String modelName, int timeoutSeconds,
                                       AiCopilotProvider fallbackProvider, HttpClient httpClient) {
        this.apiKey = (apiKey != null && !apiKey.isBlank()) ? apiKey.trim() : resolveApiKey();
        this.modelName = (modelName != null && !modelName.isBlank()) ? modelName.trim() : resolveModel();
        this.timeoutSeconds = timeoutSeconds > 0 ? timeoutSeconds : DEFAULT_TIMEOUT_SECONDS;
        this.fallbackProvider = fallbackProvider != null ? fallbackProvider : new RuleEngineCopilotProvider();
        this.httpClient = httpClient != null ? httpClient : HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(this.timeoutSeconds))
                .build();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.planMapper = new CopilotPlanMapper();

        boolean keyConfigured = isAvailable();
        LOGGER.info(String.format("OpenRouter API key configured: %b", keyConfigured));
    }

    public OpenRouterAiCopilotProvider(String apiKey) {
        this(apiKey, null, DEFAULT_TIMEOUT_SECONDS, new RuleEngineCopilotProvider(), null);
    }

    public OpenRouterAiCopilotProvider() {
        this(null, null, DEFAULT_TIMEOUT_SECONDS, new RuleEngineCopilotProvider(), null);
    }

    private static String resolveApiKey() {
        String key = System.getProperty("openrouter.api.key");
        if (key == null || key.isBlank()) {
            key = System.getenv("OPENROUTER_API_KEY");
        }
        if (key == null || key.isBlank()) {
            key = System.getenv("OPEN_ROUTER_API_KEY");
        }
        return key != null ? key.trim() : "";
    }

    private static String resolveModel() {
        String m = System.getProperty("openrouter.model");
        if (m == null || m.isBlank()) {
            m = System.getenv("OPENROUTER_MODEL");
        }
        return m != null && !m.isBlank() ? m.trim() : DEFAULT_MODEL;
    }

    @Override
    public String getProviderName() {
        return "OpenRouter AI (" + modelName + ")";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * Minimal connectivity test verifying OpenRouter API response before running full itinerary synthesis.
     */
    public boolean testMinimalConnectivity() {
        if (!isAvailable()) {
            LOGGER.info("[OpenRouter] API key configured = false, skipping minimal connectivity test.");
            return false;
        }
        try {
            LOGGER.info("[OpenRouter] provider selected");
            LOGGER.info("[OpenRouter] model = " + modelName);
            LOGGER.info("[OpenRouter] API key configured = " + isAvailable());
            LOGGER.info("[OpenRouter] sending request");

            Map<String, Object> payload = Map.of(
                    "model", modelName,
                    "messages", List.of(
                            Map.of("role", "user", "content", "Reply with exactly this JSON: {\"status\":\"ok\",\"message\":\"ERRORCab OpenRouter test successful\"}")
                    ),
                    "temperature", 0.1
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(DEFAULT_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", "https://errorcab.com")
                    .header("X-Title", "ERRORCab Travel Copilot")
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("[OpenRouter] HTTP status = " + response.statusCode());
            LOGGER.info("[OpenRouter] response received");

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String content = root.path("choices").path(0).path("message").path("content").asText();
                String clean = cleanJsonText(content);
                JsonNode parsed = objectMapper.readTree(clean);
                boolean success = "ok".equalsIgnoreCase(parsed.path("status").asText());
                LOGGER.info("[OpenRouter] response parsed = " + success);
                return success;
            } else {
                LOGGER.info("[OpenRouter] response parsed = false");
                String safeSnippet = extractSanitizedErrorMessage(response.body());
                LOGGER.warning("[OpenRouter Minimal Test] Request failed with HTTP " + response.statusCode() + ": " + safeSnippet);
                return false;
            }
        } catch (Exception e) {
            LOGGER.info("[OpenRouter] response parsed = false");
            LOGGER.warning("[OpenRouter Minimal Test] Failed: " + sanitizeDiagnostic(e.getMessage()));
            return false;
        }
    }

    @Override
    public CopilotResponse generatePlan(CopilotContext context, CopilotTripRequest request) {
        if (!isAvailable()) {
            LOGGER.info("[OpenRouter] API key configured = false. Falling back to " + fallbackProvider.getProviderName());
            return fallbackProvider.generatePlan(context, request);
        }

        LOGGER.info("[OpenRouter] provider selected");
        LOGGER.info("[OpenRouter] model = " + modelName);
        LOGGER.info("[OpenRouter] API key configured = " + isAvailable());

        try {
            String prompt = buildPrompt(context, request);
            String systemPrompt = "You are ERRORCab's specialized AI Travel Copilot for India. " +
                    "Return ONLY a raw, valid JSON object matching this schema without markdown formatting or code fences:\n" +
                    "{\n" +
                    "  \"title\": \"string\",\n" +
                    "  \"summary\": \"string\",\n" +
                    "  \"itinerary\": [\n" +
                    "    {\n" +
                    "      \"timeSlot\": \"string (e.g. 09:00 AM)\",\n" +
                    "      \"title\": \"string\",\n" +
                    "      \"locationName\": \"string\",\n" +
                    "      \"category\": \"Sightseeing | Dining | Transit | Activity\",\n" +
                    "      \"description\": \"string\",\n" +
                    "      \"rideSuggested\": true,\n" +
                    "      \"pickupLocation\": \"string\",\n" +
                    "      \"dropoffLocation\": \"string\",\n" +
                    "      \"rideReason\": \"string\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"foodRecommendations\": [\"string\"],\n" +
                    "  \"travelTips\": [\"string\"],\n" +
                    "  \"specialties\": [\"string\"],\n" +
                    "  \"warnings\": [\"string\"]\n" +
                    "}";

            Map<String, Object> payload = Map.of(
                    "model", modelName,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.3
            );

            String requestJson = objectMapper.writeValueAsString(payload);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(DEFAULT_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", "https://errorcab.com")
                    .header("X-Title", "ERRORCab Travel Copilot")
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            LOGGER.info("[OpenRouter] sending request");
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            LOGGER.info("[OpenRouter] HTTP status = " + response.statusCode());
            LOGGER.info("[OpenRouter] response received");

            if (response.statusCode() != 200) {
                int status = response.statusCode();
                String safeBodySnippet = extractSanitizedErrorMessage(response.body());
                String diagMsg;
                if (status == 404) {
                    diagMsg = String.format("[OpenRouter Error 404] (Model: '%s', Endpoint: '%s') - Cause: Invalid model identifier, unavailable model/variant, or invalid endpoint. Details: %s",
                            modelName, DEFAULT_ENDPOINT, safeBodySnippet);
                } else if (status == 401 || status == 403) {
                    diagMsg = String.format("[OpenRouter Error %d] Authentication failure. Verify OPENROUTER_API_KEY. Details: %s",
                            status, safeBodySnippet);
                } else {
                    diagMsg = String.format("[OpenRouter Error %d] (Model: '%s') Details: %s",
                            status, modelName, safeBodySnippet);
                }
                LOGGER.warning(diagMsg);
                throw new IllegalStateException(diagMsg);
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText();
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("OpenRouter returned empty content in choices[0].message.content");
            }

            String cleanedContent = cleanJsonText(content);
            GeminiPlanPayload planPayload;
            try {
                planPayload = objectMapper.readValue(cleanedContent, GeminiPlanPayload.class);
            } catch (Exception parseEx) {
                LOGGER.warning("[OpenRouter] JSON parsing failed: " + parseEx.getMessage());
                throw parseEx;
            }

            // Fill safe defaults if any optional top-level string was omitted by the LLM
            if (planPayload.getTitle() == null || planPayload.getTitle().isBlank()) {
                planPayload.setTitle("Custom Itinerary for " + (request != null && request.getDestination() != null ? request.getDestination() : "India"));
            }
            if (planPayload.getSummary() == null || planPayload.getSummary().isBlank()) {
                planPayload.setSummary("Personalized travel plan with verified ERRORCab transit options.");
            }
            if (planPayload.getItinerary() == null || planPayload.getItinerary().isEmpty()) {
                throw new IllegalArgumentException("OpenRouter response contains an empty itinerary");
            }
            planPayload.validate();

            LOGGER.info("[OpenRouter] response parsed = true");

            // Convert to CopilotResponse using provider-agnostic mapper (NO Gemini instantiation)
            CopilotResponse res = planMapper.mapPayloadToCopilotResponse(planPayload, context, request, getProviderName());
            res.setProviderName(getProviderName());
            res.setAssistanceType("AI_ASSISTED");
            return res;

        } catch (Exception e) {
            LOGGER.info("[OpenRouter] response parsed = false");
            LOGGER.log(Level.WARNING, "OpenRouter AI generation failed: {0}. Invoking emergency local fallback.",
                    sanitizeDiagnostic(e.getMessage()));
            return fallbackProvider.generatePlan(context, request);
        }
    }

    public static String cleanJsonText(String text) {
        if (text == null) return "";
        String trimmed = text.trim();
        // Remove markdown code fences if present
        if (trimmed.contains("```")) {
            int firstOpen = trimmed.indexOf("```");
            int firstClose = trimmed.indexOf("\n", firstOpen);
            int lastClose = trimmed.lastIndexOf("```");
            if (firstClose != -1 && lastClose > firstClose) {
                trimmed = trimmed.substring(firstClose + 1, lastClose).trim();
            }
        }
        // Extract outermost JSON object { ... }
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace > firstBrace) {
            trimmed = trimmed.substring(firstBrace, lastBrace + 1).trim();
        }
        return trimmed.trim();
    }

    private String extractSanitizedErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "No response body received";
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode errorNode = root.path("error");
            if (errorNode.isObject() && errorNode.has("message")) {
                return sanitizeDiagnostic(errorNode.path("message").asText());
            }
            if (root.has("message")) {
                return sanitizeDiagnostic(root.path("message").asText());
            }
        } catch (Exception ignored) {
        }
        String snippet = responseBody.replaceAll("[\\r\\n]+", " ").trim();
        if (snippet.length() > 200) {
            snippet = snippet.substring(0, 200) + "...";
        }
        return sanitizeDiagnostic(snippet);
    }

    public static String sanitizeDiagnostic(String input) {
        if (input == null) return "";
        String clean = input.replaceAll("(?i)(bearer\\s+)[a-zA-Z0-9_\\-\\.]+", "$1[REDACTED]");
        clean = clean.replaceAll("sk-[a-zA-Z0-9_\\-]{10,}", "sk-[REDACTED]");
        clean = clean.replaceAll("(?i)(key\\s*[:=]\\s*)[a-zA-Z0-9_\\-]+", "$1[REDACTED]");
        return clean.trim();
    }

    private String buildPrompt(CopilotContext context, CopilotTripRequest request) {
        String dest = request != null && request.getDestination() != null ? request.getDestination() : "Kochi";
        String purpose = request != null && request.getTripPurpose() != null ? request.getTripPurpose() : "Leisure";
        String duration = request != null && request.getDuration() != null ? request.getDuration() : "Full-day";
        String budget = request != null && request.getBudget() != null ? request.getBudget() : "Moderate";
        return String.format("Create a complete travel plan for %s (Purpose: %s, Duration: %s, Budget: %s). Return ONLY the JSON object.",
                dest, purpose, duration, budget);
    }

    public String getModelName() {
        return modelName;
    }
}
