package com.errorcab;

import com.errorcab.copilot.model.CopilotContext;
import com.errorcab.copilot.model.CopilotResponse;
import com.errorcab.copilot.model.CopilotTripRequest;
import com.errorcab.copilot.provider.GroqAiCopilotProvider;
import com.errorcab.copilot.provider.OpenRouterAiCopilotProvider;
import com.errorcab.copilot.provider.RuleEngineCopilotProvider;
import com.errorcab.copilot.service.AiCopilotService;
import com.errorcab.database.DatabaseManager;
import com.errorcab.database.DatabaseSeeder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the multi-provider abstraction: local (offline default), groq, openrouter, and gemini.
 * Validates fallback mechanisms, mock execution, provider selection, and status reporting.
 */
public class MultiProviderCopilotTest {

    @BeforeAll
    public static void setup() {
        DatabaseManager.getInstance();
        DatabaseSeeder.seedIfEmpty();
    }

    @Test
    public void testLocalProviderIsDefaultAndAlwaysAvailable() {
        AiCopilotService service = new AiCopilotService();
        service.setConfiguredProvider("local");

        assertNotNull(service.getActiveProvider());
        assertTrue(service.getActiveProvider() instanceof RuleEngineCopilotProvider);
        assertEquals("local", service.getConfiguredProvider());

        Map<String, Object> status = service.getStatus();
        assertEquals("OFFLINE_RULE_ENGINE", status.get("mode"));
        assertEquals("local", status.get("configuredProvider"));
        assertEquals(true, status.get("fallbackAvailable"));
    }

    @Test
    public void testMissingApiKeyFallsBackToLocalRuleEngineSilently() {
        AiCopilotService service = new AiCopilotService();

        // 1. Groq without key
        service.setGroqApiKey("");
        service.setConfiguredProvider("groq");
        assertTrue(service.getActiveProvider() instanceof RuleEngineCopilotProvider);

        // 2. OpenRouter without key
        service.setOpenrouterApiKey("");
        service.setConfiguredProvider("openrouter");
        assertTrue(service.getActiveProvider() instanceof RuleEngineCopilotProvider);

        // 3. Gemini without key
        service.setGeminiApiKey("");
        service.setConfiguredProvider("gemini");
        assertTrue(service.getActiveProvider() instanceof RuleEngineCopilotProvider);
    }

    @Test
    public void testGroqProviderSuccessfulExecutionWithMockClient() throws Exception {
        RuleEngineCopilotProvider fallback = new RuleEngineCopilotProvider();
        HttpClient mockHttpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        String sampleGroqJson = "{\n" +
                "  \"id\": \"chatcmpl-123\",\n" +
                "  \"choices\": [{\n" +
                "    \"message\": {\n" +
                "      \"role\": \"assistant\",\n" +
                "      \"content\": \"{\\\"title\\\": \\\"Historic Fort Kochi Day Tour\\\", \\\"summary\\\": \\\"A leisurely walk through colonial streets.\\\", \\\"itinerary\\\": [{\\\"timeSlot\\\": \\\"09:00 AM\\\", \\\"title\\\": \\\"Chinese Nets\\\", \\\"locationName\\\": \\\"Fort Kochi Beach\\\", \\\"category\\\": \\\"Sightseeing\\\", \\\"description\\\": \\\"Watch fishing nets in action.\\\"}], \\\"foodRecommendations\\\": [\\\"Appam with Stew\\\"], \\\"travelTips\\\": [\\\"Carry cash\\\"], \\\"specialties\\\": [\\\"Spice Trade\\\"], \\\"warnings\\\": [\\\"Mind traffic\\\"]}\"\n" +
                "    }\n" +
                "  }]\n" +
                "}";

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(sampleGroqJson);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        GroqAiCopilotProvider groqProvider = new GroqAiCopilotProvider(
                "gsk_test_mock_key_12345",
                "llama-3.3-70b-versatile",
                10,
                fallback,
                mockHttpClient
        );

        assertTrue(groqProvider.isAvailable());
        assertEquals("Groq AI Cloud (llama-3.3-70b-versatile)", groqProvider.getProviderName());

        CopilotTripRequest request = new CopilotTripRequest(1, "Leisure", "Fort Kochi", "Half-day", "Moderate");
        CopilotContext context = new CopilotContext();
        context.setPassengerId(1);
        context.setResolvedPickupLocation("Kakkanad");

        CopilotResponse response = groqProvider.generatePlan(context, request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Historic Fort Kochi Day Tour", response.getTitle());
        assertEquals("AI_ASSISTED", response.getAssistanceType());
        assertEquals("Groq AI Cloud (llama-3.3-70b-versatile)", response.getProviderName());
        assertFalse(response.getItinerary().isEmpty());
    }

    @Test
    public void testOpenRouterProviderSuccessfulExecutionWithMockClient() throws Exception {
        RuleEngineCopilotProvider fallback = new RuleEngineCopilotProvider();
        HttpClient mockHttpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        String sampleOpenRouterJson = "{\n" +
                "  \"id\": \"gen-456\",\n" +
                "  \"choices\": [{\n" +
                "    \"message\": {\n" +
                "      \"role\": \"assistant\",\n" +
                "      \"content\": \"{\\\"title\\\": \\\"Munnar Tea Trail Excursion\\\", \\\"summary\\\": \\\"Scenic tea hills and misty trails.\\\", \\\"itinerary\\\": [{\\\"timeSlot\\\": \\\"08:30 AM\\\", \\\"title\\\": \\\"Tea Museum\\\", \\\"locationName\\\": \\\"Munnar\\\", \\\"category\\\": \\\"Sightseeing\\\", \\\"description\\\": \\\"Learn orthodox tea processing.\\\"}], \\\"foodRecommendations\\\": [\\\"Cardamom Chai\\\"], \\\"travelTips\\\": [\\\"Wear warm jacket\\\"], \\\"specialties\\\": [\\\"High Altitude Tea\\\"], \\\"warnings\\\": [\\\"Hairpin bends on ghat road\\\"]}\"\n" +
                "    }\n" +
                "  }]\n" +
                "}";

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(sampleOpenRouterJson);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        OpenRouterAiCopilotProvider openRouterProvider = new OpenRouterAiCopilotProvider(
                "sk-or-v1-test-key",
                "openrouter/free",
                12,
                fallback,
                mockHttpClient
        );

        assertTrue(openRouterProvider.isAvailable());
        assertEquals("OpenRouter AI (openrouter/free)", openRouterProvider.getProviderName());

        CopilotTripRequest request = new CopilotTripRequest(1, "Tourism", "Munnar", "Full-day", "Moderate");
        CopilotContext context = new CopilotContext();
        context.setPassengerId(1);
        context.setResolvedPickupLocation("Kakkanad");

        CopilotResponse response = openRouterProvider.generatePlan(context, request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Munnar Tea Trail Excursion", response.getTitle());
        assertEquals("AI_ASSISTED", response.getAssistanceType());
        assertEquals("OpenRouter AI (openrouter/free)", response.getProviderName());
    }

    @Test
    public void testOpenRouterProvider404FallsBackSeamlesslyToRuleEngine() throws Exception {
        RuleEngineCopilotProvider fallback = new RuleEngineCopilotProvider();
        HttpClient mockHttpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        when(mockResponse.statusCode()).thenReturn(404);
        when(mockResponse.body()).thenReturn("{\"error\":{\"message\":\"No such model: invalid-model\",\"code\":404}}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        OpenRouterAiCopilotProvider openRouterProvider = new OpenRouterAiCopilotProvider(
                "sk-or-v1-test-key",
                "openrouter/free",
                12,
                fallback,
                mockHttpClient
        );

        CopilotTripRequest request = new CopilotTripRequest(1, "Tourism", "Munnar", "Full-day", "Moderate");
        CopilotContext context = new CopilotContext();
        context.setPassengerId(1);
        context.setResolvedPickupLocation("Kakkanad");

        // When OpenRouter returns 404, provider must NOT throw; it falls back to Rule Engine
        CopilotResponse response = openRouterProvider.generatePlan(context, request);
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Munnar", response.getDestination());
        assertEquals("SMART_OFFLINE", response.getAssistanceType());
        assertEquals("ERRORCab Local Rule Engine (Offline Foundation)", response.getProviderName());
    }

    @Test
    public void testOpenRouterDiagnosticSanitizationNeverExposesSecrets() {
        String sensitive = "Error with Bearer sk-or-v1-abcdef1234567890 and key=secret_val_12345";
        String sanitized = OpenRouterAiCopilotProvider.sanitizeDiagnostic(sensitive);
        assertFalse(sanitized.contains("abcdef1234567890"));
        assertFalse(sanitized.contains("secret_val_12345"));
        assertTrue(sanitized.contains("[REDACTED]"));
    }

    @Test
    public void testExternalProviderFailureGracefullyFallsBackToRuleEngine() throws Exception {
        RuleEngineCopilotProvider fallback = new RuleEngineCopilotProvider();
        HttpClient mockHttpClient = mock(HttpClient.class);

        // Simulate network failure or timeout
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Simulated Connection Reset / 503 Gateway Timeout"));

        GroqAiCopilotProvider groqProvider = new GroqAiCopilotProvider(
                "gsk_test_mock_key_12345",
                "llama-3.3-70b-versatile",
                5,
                fallback,
                mockHttpClient
        );

        CopilotTripRequest request = new CopilotTripRequest(1, "Leisure", "Bekal", "Full-day", "Moderate");
        CopilotContext context = new CopilotContext();
        context.setPassengerId(1);
        context.setResolvedPickupLocation("Kannur");

        // Provider must NOT crash; it must silently fall back to rule engine
        CopilotResponse response = groqProvider.generatePlan(context, request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Bekal", response.getDestination());
        assertEquals("SMART_OFFLINE", response.getAssistanceType());
        assertEquals("ERRORCab Local Rule Engine (Offline Foundation)", response.getProviderName());
        assertFalse(response.getItinerary().isEmpty());
    }

    @Test
    public void testOpenRouterMinimalConnectivitySucceedsWithMockClient() throws Exception {
        RuleEngineCopilotProvider fallback = new RuleEngineCopilotProvider();
        HttpClient mockHttpClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        String sampleResp = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"{\\\"status\\\":\\\"ok\\\",\\\"message\\\":\\\"ERRORCab OpenRouter test successful\\\"}\"}}]}";
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(sampleResp);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        OpenRouterAiCopilotProvider provider = new OpenRouterAiCopilotProvider(
                "sk-or-v1-test-key",
                "openrouter/free",
                12,
                fallback,
                mockHttpClient
        );

        boolean result = provider.testMinimalConnectivity();
        assertTrue(result);
    }
}
