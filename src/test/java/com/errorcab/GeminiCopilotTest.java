package com.errorcab;

import com.errorcab.copilot.gemini.dto.GeminiCandidate;
import com.errorcab.copilot.gemini.dto.GeminiContent;
import com.errorcab.copilot.gemini.dto.GeminiGenerateRequest;
import com.errorcab.copilot.gemini.dto.GeminiGenerateResponse;
import com.errorcab.copilot.gemini.dto.GeminiGenerationConfig;
import com.errorcab.copilot.gemini.dto.GeminiLegPayload;
import com.errorcab.copilot.gemini.dto.GeminiPart;
import com.errorcab.copilot.gemini.dto.GeminiPlanPayload;
import com.errorcab.copilot.model.CopilotContext;
import com.errorcab.copilot.model.CopilotResponse;
import com.errorcab.copilot.model.CopilotRideSuggestion;
import com.errorcab.copilot.model.CopilotTripRequest;
import com.errorcab.copilot.provider.GeminiAiCopilotProvider;
import com.errorcab.copilot.provider.RuleEngineCopilotProvider;
import com.errorcab.copilot.service.AiCopilotService;
import com.errorcab.database.DatabaseManager;
import com.errorcab.database.DatabaseSeeder;
import com.errorcab.model.Booking;
import com.errorcab.model.CabType;
import com.errorcab.model.FavoriteLocation;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Stage 2 automated tests for Google Gemini AI Copilot Integration.
 * Validates request/response DTOs, thoughtSignature & unknown property handling,
 * candidate text extraction, payload schema validation, automatic retry on 503/429,
 * fallback on malformed JSON or missing API key, and provider selection with gemini-3.5-flash.
 */
public class GeminiCopilotTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeAll
    public static void setup() {
        DatabaseManager.getInstance();
        DatabaseSeeder.seedIfEmpty();
    }

    @Test
    public void testGeminiRequestAndResponseDtoSerialization() throws Exception {
        // Request DTO serialization
        GeminiGenerateRequest req = new GeminiGenerateRequest();
        req.getContents().add(GeminiContent.userContent("Plan a day in Fort Kochi"));
        req.setSystemInstruction(GeminiContent.systemContent("You are a travel assistant."));
        req.setGenerationConfig(GeminiGenerationConfig.jsonConfig(0.3));

        String json = objectMapper.writeValueAsString(req);
        assertNotNull(json);
        assertTrue(json.contains("\"role\":\"user\""));
        assertTrue(json.contains("\"responseMimeType\":\"application/json\""));

        // Response DTO deserialization
        String sampleResponseJson = "{\n" +
                "  \"candidates\": [\n" +
                "    {\n" +
                "      \"content\": {\n" +
                "        \"parts\": [{\"text\": \"{\\\"title\\\": \\\"Kochi Tour\\\"}\"}],\n" +
                "        \"role\": \"model\"\n" +
                "      },\n" +
                "      \"finishReason\": \"STOP\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        GeminiGenerateResponse resp = objectMapper.readValue(sampleResponseJson, GeminiGenerateResponse.class);
        assertNotNull(resp);
        assertEquals(1, resp.getCandidates().size());
        assertEquals("{\"title\": \"Kochi Tour\"}", resp.getFirstCandidateText());
    }

    @Test
    public void testGeminiResponseWithThoughtSignatureAndUnknownFields() throws Exception {
        // Simulates Gemini 3.5 Flash response containing thought parts, thoughtSignature, and extra metadata
        String thinkingResponseJson = "{\n" +
                "  \"candidates\": [\n" +
                "    {\n" +
                "      \"content\": {\n" +
                "        \"parts\": [\n" +
                "          {\n" +
                "            \"thought\": true,\n" +
                "            \"thoughtSignature\": \"AQACAAQAk0h1234567890abcdef==\",\n" +
                "            \"text\": \"Thinking Process: Planning a culinary and heritage tour for Fort Kochi...\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"thoughtSignature\": \"AQACAAQAk0h1234567890abcdef==\",\n" +
                "            \"text\": \"{\\\"title\\\": \\\"Fort Kochi Heritage\\\", \\\"summary\\\": \\\"Historical trip\\\", \\\"itinerary\\\": [{\\\"title\\\": \\\"Promenade Walk\\\"}]}\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"role\": \"model\",\n" +
                "        \"unknownContentField\": \"shouldBeIgnored\"\n" +
                "      },\n" +
                "      \"finishReason\": \"STOP\",\n" +
                "      \"index\": 0,\n" +
                "      \"safetyRatings\": [\n" +
                "        {\"category\": \"HARM_CATEGORY_HARASSMENT\", \"probability\": \"NEGLIGIBLE\"}\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"usageMetadata\": {\n" +
                "    \"promptTokenCount\": 120,\n" +
                "    \"candidatesTokenCount\": 340,\n" +
                "    \"totalTokenCount\": 460\n" +
                "  },\n" +
                "  \"modelVersion\": \"gemini-3.5-flash\"\n" +
                "}";

        GeminiGenerateResponse resp = objectMapper.readValue(thinkingResponseJson, GeminiGenerateResponse.class);
        assertNotNull(resp);
        assertEquals(1, resp.getCandidates().size());

        GeminiCandidate candidate = resp.getCandidates().get(0);
        assertEquals(2, candidate.getContent().getParts().size());

        GeminiPart thoughtPart = candidate.getContent().getParts().get(0);
        assertTrue(Boolean.TRUE.equals(thoughtPart.getThought()));
        assertEquals("AQACAAQAk0h1234567890abcdef==", thoughtPart.getThoughtSignature());

        GeminiPart contentPart = candidate.getContent().getParts().get(1);
        assertNull(contentPart.getThought());
        assertEquals("AQACAAQAk0h1234567890abcdef==", contentPart.getThoughtSignature());

        // extractFirstText() must skip the thought part and extract the actual JSON payload
        String extracted = resp.getFirstCandidateText();
        assertNotNull(extracted);
        assertTrue(extracted.contains("\"title\": \"Fort Kochi Heritage\""));
        assertFalse(extracted.contains("Thinking Process"));
    }

    @Test
    public void testCandidateExtractFirstTextRobustness() {
        // Case 1: Null content / empty parts
        GeminiCandidate emptyCandidate = new GeminiCandidate();
        assertNull(emptyCandidate.extractFirstText());

        // Case 2: Only thought part, no content part
        GeminiCandidate thoughtOnly = new GeminiCandidate();
        GeminiContent thoughtContent = new GeminiContent();
        thoughtContent.getParts().add(new GeminiPart("Thinking...", "sig123", true));
        thoughtOnly.setContent(thoughtContent);
        // Falls back to available text
        assertEquals("Thinking...", thoughtOnly.extractFirstText());

        // Case 3: Thought part followed by valid JSON text
        GeminiCandidate validCandidate = new GeminiCandidate();
        GeminiContent validContent = new GeminiContent();
        validContent.getParts().add(new GeminiPart("Reasoning steps...", "sigA", true));
        validContent.getParts().add(new GeminiPart("{\"title\": \"Munnar Getaway\"}", "sigB", null));
        validCandidate.setContent(validContent);
        assertEquals("{\"title\": \"Munnar Getaway\"}", validCandidate.extractFirstText());

        // Case 4: No thought flags, standard single text part
        GeminiCandidate standardCandidate = new GeminiCandidate();
        GeminiContent standardContent = new GeminiContent();
        standardContent.getParts().add(new GeminiPart("{\"title\": \"Aluva Tour\"}"));
        standardCandidate.setContent(standardContent);
        assertEquals("{\"title\": \"Aluva Tour\"}", standardCandidate.extractFirstText());
    }

    @Test
    public void testSanitizeResponseBodyRedactsThoughtSignatureAndKeys() {
        String rawBody = "{\"candidates\":[{\"content\":{\"parts\":[{\"thoughtSignature\":\"AQACAAQAk0hLongSignatureSecretToken123456==\",\"text\":\"Hello\"}]}}]}";
        String sanitized = GeminiAiCopilotProvider.sanitizeResponseBody(rawBody);

        assertFalse(sanitized.contains("AQACAAQAk0hLongSignatureSecretToken123456=="));
        assertTrue(sanitized.contains("\"thoughtSignature\":\"[REDACTED]\""));

        // Sensitive key pattern redaction
        String rawWithKey = "Error on URL https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash?key=AIzaSyDUMMYKEY12345678901234567890123";
        String sanitizedKey = GeminiAiCopilotProvider.sanitizeResponseBody(rawWithKey);
        assertFalse(sanitizedKey.contains("AIzaSyDUMMYKEY12345678901234567890123"));
        assertTrue(sanitizedKey.contains("[REDACTED]"));
    }

    @Test
    public void testGeminiModelConfigurableViaSystemPropertyOrEnv() {
        // Default when no property is set
        assertEquals("gemini-3.5-flash", GeminiAiCopilotProvider.resolveDefaultModel());

        // Override via system property
        System.setProperty("gemini.model", "gemini-3.8-flash");
        try {
            assertEquals("gemini-3.8-flash", GeminiAiCopilotProvider.resolveDefaultModel());
            GeminiAiCopilotProvider provider = new GeminiAiCopilotProvider("test-key");
            assertEquals("gemini-3.8-flash", provider.getModelName());
            assertEquals("Google Gemini (gemini-3.8-flash)", provider.getProviderName());
        } finally {
            System.clearProperty("gemini.model");
        }

        // Restores default
        assertEquals("gemini-3.5-flash", GeminiAiCopilotProvider.resolveDefaultModel());
    }

    @Test
    public void testPromptBuilderEmbedsAllRequiredContextAndPreferences() {
        GeminiAiCopilotProvider provider = new GeminiAiCopilotProvider("dummy-key");

        CopilotContext context = new CopilotContext();
        context.setPassengerName("Rahul Nair");
        context.setResolvedPickupLocation("Kakkanad");
        context.setTotalRides(14);
        context.setTotalSpent(2850.0);
        context.getFavorites().add(new FavoriteLocation(1, 1, "Home", "Kakkanad", "Infopark IT Hub", LocalDateTime.now()));

        Booking pastTrip = new Booking(1, "EC-1001", 1, "Rahul Nair", "+91 98471 23456",
                null, null, null, null, null, 5.0,
                "Kakkanad", "Vyttila", 9.2, 24, CabType.ECONOMY, 179.0, null, null, LocalDateTime.now(), null);
        context.getRecentTrips().add(pastTrip);

        CopilotTripRequest request = new CopilotTripRequest(1, "Culinary & Heritage", "Fort Kochi", "Full-day", "Moderate");
        request.setInterests(List.of("Heritage & History", "Beaches & Sunset"));
        request.setFoodPreferences(List.of("Fresh Coastal Seafood", "Artisanal Cafes"));
        request.setActivityPreferences(List.of("Walking Tour", "Photography"));

        String prompt = provider.buildPrompt(context, request);

        // Verify context fields present
        assertTrue(prompt.contains("Rahul Nair"), "Prompt should contain passenger name");
        assertTrue(prompt.contains("Kakkanad"), "Prompt should contain default pickup hub");
        assertTrue(prompt.contains("14"), "Prompt should contain total past rides");
        assertTrue(prompt.contains("Infopark IT Hub"), "Prompt should contain favorite address");
        assertTrue(prompt.contains("Vyttila"), "Prompt should contain past trip destination");

        // Verify preference fields present
        assertTrue(prompt.contains("Culinary & Heritage"), "Prompt should contain trip purpose");
        assertTrue(prompt.contains("Fort Kochi"), "Prompt should contain destination");
        assertTrue(prompt.contains("Full-day"), "Prompt should contain duration");
        assertTrue(prompt.contains("Moderate"), "Prompt should contain budget");
        assertTrue(prompt.contains("Fresh Coastal Seafood"), "Prompt should contain food preference");
        assertTrue(prompt.contains("Beaches & Sunset"), "Prompt should contain interest");
        assertTrue(prompt.contains("Photography"), "Prompt should contain activity preference");
    }

    @Test
    public void testGeminiPlanPayloadSchemaValidation() {
        // Valid payload
        GeminiPlanPayload valid = new GeminiPlanPayload();
        valid.setTitle("Fort Kochi Weekend Tour");
        valid.setSummary("A delightful weekend trip to historic Fort Kochi.");
        GeminiLegPayload leg = new GeminiLegPayload();
        leg.setTitle("Walk on Promenade");
        valid.getItinerary().add(leg);

        assertDoesNotThrow(valid::validate);

        // Missing title
        GeminiPlanPayload noTitle = new GeminiPlanPayload();
        noTitle.setSummary("Summary only");
        noTitle.getItinerary().add(leg);
        assertThrows(IllegalArgumentException.class, noTitle::validate);

        // Empty itinerary
        GeminiPlanPayload emptyItinerary = new GeminiPlanPayload();
        emptyItinerary.setTitle("Title");
        emptyItinerary.setSummary("Summary");
        assertThrows(IllegalArgumentException.class, emptyItinerary::validate);
    }

    @Test
    public void testMapPayloadToCopilotResponseEnrichesFaresAndBookingUrls() {
        GeminiAiCopilotProvider provider = new GeminiAiCopilotProvider("dummy-key");

        CopilotContext context = new CopilotContext();
        context.setResolvedPickupLocation("Kakkanad");

        CopilotTripRequest request = new CopilotTripRequest(1, "Sightseeing", "Fort Kochi", "Half-day", "Moderate");

        GeminiPlanPayload payload = new GeminiPlanPayload();
        payload.setTitle("Fort Kochi Heritage");
        payload.setSummary("Explore the colonial architecture and fishing nets.");

        GeminiLegPayload leg1 = new GeminiLegPayload();
        leg1.setTimeSlot("09:00 AM - 10:00 AM");
        leg1.setTitle("Cab Transfer to Fort Kochi");
        leg1.setLocationName("Fort Kochi");
        leg1.setCategory("Transit");
        leg1.setPickupLocation("Kakkanad");
        leg1.setDropoffLocation("Fort Kochi");
        leg1.setCabType("PREMIUM");
        leg1.setRideSuggested(true);

        GeminiLegPayload leg2 = new GeminiLegPayload();
        leg2.setTimeSlot("10:15 AM - 12:00 PM");
        leg2.setTitle("Chinese Fishing Nets Walk");
        leg2.setLocationName("Fort Kochi Promenade");
        leg2.setCategory("Sightseeing");
        leg2.setDescription("View ancient fishing structures.");

        payload.getItinerary().add(leg1);
        payload.getItinerary().add(leg2);
        payload.getFoodRecommendations().add("Seagull Waterfront Seafood");
        payload.getTravelTips().add("Carry light cotton clothing.");
        payload.getSpecialties().add("Historic Chinese Fishing Nets");
        payload.getWarnings().add("Check Synagogue timings before visiting.");

        CopilotResponse response = provider.mapPayloadToCopilotResponse(payload, context, request);

        assertNotNull(response);
        assertEquals("Fort Kochi Heritage", response.getTitle());
        assertEquals("Google Gemini (gemini-3.5-flash)", response.getProviderName());
        assertEquals(2, response.getItinerary().size());
        assertEquals(1, response.getRecommendedRides().size());
        assertEquals(1, response.getSpecialties().size());
        assertEquals("Historic Chinese Fishing Nets", response.getSpecialties().get(0));
        assertEquals(1, response.getWarnings().size());
        assertEquals("Check Synagogue timings before visiting.", response.getWarnings().get(0));

        CopilotRideSuggestion ride = response.getRecommendedRides().get(0);
        assertEquals("Kakkanad", ride.getPickupLocation());
        assertEquals("Fort Kochi", ride.getDropoffLocation());
        assertEquals(CabType.PREMIUM, ride.getRecommendedCabType());
        assertTrue(ride.getDistanceKm() > 0);
        assertTrue(ride.getEstimatedFare() > 0);
        assertTrue(ride.getBookingUrl().startsWith("/booking.html?pickup=Kakkanad&dropoff=Fort+Kochi&cabType=PREMIUM"),
                "Booking URL must deep link to existing booking flow");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSuccessfulGeminiResponseParsingWithThoughtSignature() throws Exception {
        RuleEngineCopilotProvider ruleEngine = new RuleEngineCopilotProvider();
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        String successBody = "{\n" +
                "  \"candidates\": [{\n" +
                "    \"content\": {\n" +
                "      \"parts\": [\n" +
                "        {\n" +
                "          \"thought\": true,\n" +
                "          \"thoughtSignature\": \"sigThinking123\",\n" +
                "          \"text\": \"Analyzing passenger preferences for Fort Kochi...\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"thoughtSignature\": \"sigContent456\",\n" +
                "          \"text\": \"{\\\"title\\\": \\\"Fort Kochi Heritage Trail\\\", \\\"summary\\\": \\\"Historic trail in Kochi\\\", \\\"itinerary\\\": [{\\\"timeSlot\\\": \\\"09:00 AM - 10:00 AM\\\", \\\"title\\\": \\\"Fort Kochi Beach\\\", \\\"locationName\\\": \\\"Fort Kochi Promenade\\\", \\\"category\\\": \\\"Sightseeing\\\", \\\"description\\\": \\\"Scenic coastline.\\\"}]}\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"role\": \"model\"\n" +
                "    },\n" +
                "    \"finishReason\": \"STOP\"\n" +
                "  }]\n" +
                "}";

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(successBody);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        GeminiAiCopilotProvider provider = new GeminiAiCopilotProvider(
                "test-api-key", "gemini-3.5-flash", 5, ruleEngine, mockHttpClient
        );

        CopilotContext context = new CopilotContext();
        context.setPassengerName("Rahul Nair");
        context.setResolvedPickupLocation("Kakkanad");

        CopilotTripRequest request = new CopilotTripRequest(1, "Tourism", "Fort Kochi", "Half-day", "Moderate");

        CopilotResponse response = provider.generatePlan(context, request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Google Gemini (gemini-3.5-flash)", response.getProviderName());
        assertEquals("Fort Kochi Heritage Trail", response.getTitle());
        assertFalse(response.getItinerary().isEmpty());
    }

    @Test
    public void testFallbackWhenApiKeyIsMissingOrBlank() {
        RuleEngineCopilotProvider ruleEngine = new RuleEngineCopilotProvider();
        GeminiAiCopilotProvider providerNoKey = new GeminiAiCopilotProvider("", "gemini-3.5-flash", 12, ruleEngine, null);

        assertFalse(providerNoKey.isAvailable());

        CopilotContext context = new CopilotContext();
        context.setPassengerName("Rahul Nair");
        context.setResolvedPickupLocation("Kakkanad");

        CopilotTripRequest request = new CopilotTripRequest(1, "Tourism", "Fort Kochi", "Half-day", "Budget");

        // Must automatically delegate to fallback provider without throwing
        CopilotResponse response = providerNoKey.generatePlan(context, request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getSpecialties());
        assertFalse(response.getSpecialties().isEmpty());
        assertNotNull(response.getWarnings());
        assertFalse(response.getWarnings().isEmpty());
        assertEquals("ERRORCab Local Rule Engine (Offline Foundation)", response.getProviderName());
        assertEquals("Fort Kochi", response.getDestination());
        assertFalse(response.getRecommendedRides().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFallbackOnSimulatedGeminiHttpError() throws Exception {
        RuleEngineCopilotProvider ruleEngine = new RuleEngineCopilotProvider();
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("{\"error\": \"Internal Server Error\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        GeminiAiCopilotProvider providerWithMock = new GeminiAiCopilotProvider(
                "test-api-key", "gemini-3.5-flash", 5, ruleEngine, mockHttpClient
        );

        assertTrue(providerWithMock.isAvailable());

        CopilotContext context = new CopilotContext();
        context.setPassengerName("Rahul Nair");
        context.setResolvedPickupLocation("Kakkanad");

        CopilotTripRequest request = new CopilotTripRequest(1, "Leisure", "Edappally", "Full-day", "Moderate");

        // The HTTP 500 error should be caught and automatically fallen back to Rule Engine
        CopilotResponse response = providerWithMock.generatePlan(context, request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("ERRORCab Local Rule Engine (Offline Foundation)", response.getProviderName());
        assertEquals("Edappally", response.getDestination());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFallbackOnMalformedJsonFromGemini() throws Exception {
        RuleEngineCopilotProvider ruleEngine = new RuleEngineCopilotProvider();
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        // 200 OK from Gemini, but candidates contain invalid/unparseable JSON text
        String malformedResponseBody = "{\n" +
                "  \"candidates\": [{\n" +
                "    \"content\": {\n" +
                "      \"parts\": [{\"text\": \"This is clearly not a JSON object, I cannot help you.\"}]\n" +
                "    }\n" +
                "  }]\n" +
                "}";

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(malformedResponseBody);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        GeminiAiCopilotProvider provider = new GeminiAiCopilotProvider(
                "test-api-key", "gemini-3.5-flash", 5, ruleEngine, mockHttpClient
        );

        CopilotContext context = new CopilotContext();
        context.setPassengerName("Rahul Nair");
        context.setResolvedPickupLocation("Kakkanad");

        CopilotTripRequest request = new CopilotTripRequest(1, "Tourism", "Fort Kochi", "Half-day", "Moderate");

        // When JSON parsing / validation fails, must seamlessly fallback to Rule Engine
        CopilotResponse response = provider.generatePlan(context, request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("ERRORCab Local Rule Engine (Offline Foundation)", response.getProviderName());
        assertEquals("Fort Kochi", response.getDestination());
    }

    @Test
    public void testAiCopilotServiceProviderSelection() {
        RuleEngineCopilotProvider ruleEngine = new RuleEngineCopilotProvider();

        // 1. Configured as 'gemini' with NO key -> active provider must be Rule Engine
        AiCopilotService serviceNoKey = new AiCopilotService("gemini", "", "gemini-3.5-flash", null);
        assertEquals(ruleEngine.getProviderName(), serviceNoKey.getActiveProvider().getProviderName());

        // 2. Configured as 'rule-engine' -> active provider must be Rule Engine
        AiCopilotService serviceRuleChoice = new AiCopilotService("rule-engine", "some-key", "gemini-3.5-flash", null);
        assertEquals(ruleEngine.getProviderName(), serviceRuleChoice.getActiveProvider().getProviderName());

        // 3. Configured as 'gemini' WITH valid key -> active provider must be Gemini
        GeminiAiCopilotProvider customGemini = new GeminiAiCopilotProvider("valid-key-xyz", "gemini-3.5-flash");
        AiCopilotService serviceWithKey = new AiCopilotService("gemini", "valid-key-xyz", "gemini-3.5-flash", customGemini);
        assertEquals("Google Gemini (gemini-3.5-flash)", serviceWithKey.getActiveProvider().getProviderName());

        // 4. Verify getStatus output
        Map<String, Object> status = serviceWithKey.getStatus();
        assertEquals("gemini", status.get("configuredProvider"));
        assertEquals("Google Gemini (gemini-3.5-flash)", status.get("activeProvider"));
        assertEquals("gemini-3.5-flash", status.get("geminiModel"));
        assertEquals("ONLINE_GEMINI", status.get("mode"));
        assertEquals(true, status.get("geminiAvailable"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAutomaticRetryOn503AndFallbackToRuleEngine() throws Exception {
        RuleEngineCopilotProvider ruleEngine = new RuleEngineCopilotProvider();
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mock503Response = mock(HttpResponse.class);

        when(mock503Response.statusCode()).thenReturn(503);
        when(mock503Response.body()).thenReturn("{\"error\": \"This model is currently experiencing high demand\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mock503Response);

        GeminiAiCopilotProvider provider = new GeminiAiCopilotProvider(
                "test-api-key", "gemini-3.5-flash", 5, ruleEngine, mockHttpClient
        );
        provider.setBackoffBaseMs(5); // fast backoff for testing (5ms, 10ms, 20ms)

        CopilotContext context = new CopilotContext();
        context.setPassengerName("Rahul Nair");
        context.setResolvedPickupLocation("Kakkanad");

        CopilotTripRequest request = new CopilotTripRequest(1, "Tourism", "Fort Kochi", "Half-day", "Moderate");

        // When 503 persists across all 3 retries, it must gracefully fallback to RuleEngineCopilotProvider
        CopilotResponse response = provider.generatePlan(context, request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("ERRORCab Local Rule Engine (Offline Foundation)", response.getProviderName());
        assertEquals("Fort Kochi", response.getDestination());
        assertFalse(response.getItinerary().isEmpty());

        // Verify that initial call + 3 retries = 4 total calls were executed
        verify(mockHttpClient, times(4)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAutomaticRetryOn429AndFallbackToRuleEngine() throws Exception {
        RuleEngineCopilotProvider ruleEngine = new RuleEngineCopilotProvider();
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mock429Response = mock(HttpResponse.class);

        when(mock429Response.statusCode()).thenReturn(429);
        when(mock429Response.body()).thenReturn("{\"error\": {\"code\": 429, \"message\": \"Resource has been exhausted (e.g. check quota).\"}}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mock429Response);

        GeminiAiCopilotProvider provider = new GeminiAiCopilotProvider(
                "test-api-key", "gemini-3.5-flash", 5, ruleEngine, mockHttpClient
        );
        provider.setBackoffBaseMs(5); // fast backoff for testing

        CopilotContext context = new CopilotContext();
        context.setPassengerName("Rahul Nair");
        context.setResolvedPickupLocation("Kakkanad");

        CopilotTripRequest request = new CopilotTripRequest(1, "Tourism", "Fort Kochi", "Half-day", "Moderate");

        // When 429 persists across all 3 retries, it must gracefully fallback to RuleEngineCopilotProvider
        CopilotResponse response = provider.generatePlan(context, request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("ERRORCab Local Rule Engine (Offline Foundation)", response.getProviderName());
        assertEquals("Fort Kochi", response.getDestination());

        // Verify that initial call + 3 retries = 4 total calls were executed
        verify(mockHttpClient, times(4)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
}
