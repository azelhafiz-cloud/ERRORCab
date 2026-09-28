package com.errorcab.copilot.provider;

import com.errorcab.copilot.model.CopilotContext;
import com.errorcab.copilot.model.CopilotResponse;
import com.errorcab.copilot.model.CopilotTripRequest;

/**
 * Provider interface contract for travel copilot intelligence engines.
 * Enables zero-refactoring migration from the initial offline rule-engine
 * to future cloud AI providers (e.g. Gemini, OpenAI, Claude).
 */
public interface AiCopilotProvider {
    /**
     * Unique identifier for the provider (e.g., "ERRORCab Local Rule Engine", "Google Gemini Pro").
     */
    String getProviderName();

    /**
     * Checks if this provider is currently available (offline engines are always true).
     */
    boolean isAvailable();

    /**
     * Synthesizes personalized travel itinerary, food suggestions, tips,
     * and ERRORCab ride options tailored to the passenger's context and preferences.
     */
    CopilotResponse generatePlan(CopilotContext context, CopilotTripRequest request);
}
