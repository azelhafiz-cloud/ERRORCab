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
 * Free external AI provider utilizing OpenRouter API.
 * Accesses open free-tier models (e.g. Meta Llama 3.3 Free).
 * Automatically and seamlessly falls back to RuleEngineCopilotProvider upon any failure.
 */
public class OpenRouterAiCopilotProvider implements AiCopilotProvider {

    private static final Logger LOGGER = Logger.getLogger(OpenRouterAiCopilotProvider.class.getName());
    private static final String DEFAULT_ENDPOINT = "https://openrouter.ai/api/v1/chat/completions";
    private static final String DEFAULT_MODEL = "meta-llama/llama-3.3-70b-instruct:free";
    private static final int DEFAULT_TIMEOUT_SECONDS = 12;

    private final String apiKey;
    private final String modelName;
    private final int timeoutSeconds;
    private final AiCopilotProvider fallbackProvider;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenRouterAiCopilotProvider(String apiKey, String modelName, int timeoutSeconds,
                                       AiCopilotProvider fallbackProvider, HttpClient httpClient) {
        this.apiKey = apiKey != null ? apiKey.trim() : resolveApiKey();
        this.modelName = modelName != null && !modelName.isBlank() ? modelName.trim() : resolveModel();
        this.timeoutSeconds = timeoutSeconds > 0 ? timeoutSeconds : DEFAULT_TIMEOUT_SECONDS;
        this.fallbackProvider = fallbackProvider != null ? fallbackProvider : new RuleEngineCopilotProvider();
        this.httpClient = httpClient != null ? httpClient : HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(this.timeoutSeconds))
                .build();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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

    @Override
    public CopilotResponse generatePlan(CopilotContext context, CopilotTripRequest request) {
        if (!isAvailable()) {
            LOGGER.info("OpenRouter API key not configured. Falling back to " + fallbackProvider.getProviderName());
            return fallbackProvider.generatePlan(context, request);
        }

        try {
            String prompt = buildPrompt(context, request);
            String systemPrompt = "You are ERRORCab's specialized AI Travel Copilot for India and Kerala. " +
                    "Return ONLY valid JSON matching this schema: " +
                    "{\"title\": string, \"summary\": string, \"itinerary\": [{\"timeSlot\": string, \"title\": string, \"locationName\": string, \"category\": string, \"description\": string, \"rideSuggested\": boolean}], \"specialties\": [string], \"warnings\": [string], \"foodRecommendations\": [string], \"travelTips\": [string]}";

            Map<String, Object> payload = Map.of(
                    "model", modelName,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "response_format", Map.of("type", "json_object"),
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

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("OpenRouter API error status " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText();
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("OpenRouter returned empty content");
            }

            GeminiPlanPayload planPayload = objectMapper.readValue(content, GeminiPlanPayload.class);
            planPayload.validate();

            GeminiAiCopilotProvider mapper = new GeminiAiCopilotProvider("dummy", modelName);
            CopilotResponse res = mapper.mapPayloadToCopilotResponse(planPayload, context, request);
            res.setProviderName(getProviderName());
            res.setAssistanceType("AI_ASSISTED");
            return res;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "OpenRouter AI generation failed: {0}. Invoking emergency local fallback.", e.getMessage());
            return fallbackProvider.generatePlan(context, request);
        }
    }

    private String buildPrompt(CopilotContext context, CopilotTripRequest request) {
        String dest = request.getDestination() != null ? request.getDestination() : "Kochi";
        String purpose = request.getTripPurpose() != null ? request.getTripPurpose() : "Leisure";
        String duration = request.getDuration() != null ? request.getDuration() : "Half-day";
        return "Create a travel plan for " + dest + " (" + purpose + ", " + duration + ").";
    }

    public String getModelName() {
        return modelName;
    }
}
