package com.errorcab.copilot.service;

import com.errorcab.config.AppConfig;
import com.errorcab.copilot.destination.service.DestinationKnowledgeBase;
import com.errorcab.copilot.model.CopilotContext;
import com.errorcab.copilot.model.CopilotResponse;
import com.errorcab.copilot.model.CopilotTripRequest;
import com.errorcab.copilot.provider.AiCopilotProvider;
import com.errorcab.copilot.provider.GeminiAiCopilotProvider;
import com.errorcab.copilot.provider.GroqAiCopilotProvider;
import com.errorcab.copilot.provider.OpenRouterAiCopilotProvider;
import com.errorcab.copilot.provider.RuleEngineCopilotProvider;
import com.errorcab.model.Booking;
import com.errorcab.model.CabType;
import com.errorcab.model.Driver;
import com.errorcab.model.FavoriteLocation;
import com.errorcab.model.User;
import com.errorcab.repository.BookingRepository;
import com.errorcab.repository.DriverRepository;
import com.errorcab.repository.FavoriteRepository;
import com.errorcab.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service orchestrating ERRORCab AI — Travel Copilot.
 * Pluggable multi-provider architecture supporting:
 * - local (Rule Engine with Destination Intelligence)
 * - groq (High-speed Llama 3 cloud)
 * - openrouter (Free tier open models)
 * - gemini (Google Gemini Flash)
 * Always prioritizes guaranteed offline execution with zero-downtime silent fallback.
 */
@Service
public class AiCopilotService {

    private static final Logger LOGGER = Logger.getLogger(AiCopilotService.class.getName());

    private final UserRepository userRepository = new UserRepository();
    private final FavoriteRepository favoriteRepository = new FavoriteRepository();
    private final BookingRepository bookingRepository = new BookingRepository();
    private final DriverRepository driverRepository = new DriverRepository();

    @Value("${copilot.provider:${COPILOT_PROVIDER:local}}")
    private String configuredProvider = resolveConfiguredProvider();

    @Value("${gemini.api.key:}")
    private String geminiApiKey = "";

    @Value("${gemini.model:${GEMINI_MODEL:gemini-3.5-flash}}")
    private String geminiModel = GeminiAiCopilotProvider.resolveDefaultModel();

    @Value("${gemini.timeout-seconds:12}")
    private int geminiTimeoutSeconds = 12;

    @Value("${groq.api.key:}")
    private String groqApiKey = "";

    @Value("${groq.model:${GROQ_MODEL:llama-3.3-70b-versatile}}")
    private String groqModel = "llama-3.3-70b-versatile";

    @Value("${openrouter.api.key:}")
    private String openrouterApiKey = "";

    @Value("${openrouter.model:${OPENROUTER_MODEL:meta-llama/llama-3.3-70b-instruct:free}}")
    private String openrouterModel = "meta-llama/llama-3.3-70b-instruct:free";

    private RuleEngineCopilotProvider ruleEngineProvider = new RuleEngineCopilotProvider();
    private GeminiAiCopilotProvider geminiProvider;
    private GroqAiCopilotProvider groqProvider;
    private OpenRouterAiCopilotProvider openRouterProvider;
    private AiCopilotProvider activeProvider;

    public AiCopilotService() {
        initProvider();
    }

    public AiCopilotService(AiCopilotProvider provider) {
        if (provider != null) {
            this.activeProvider = provider;
        } else {
            initProvider();
        }
    }

    public AiCopilotService(String configuredProvider, String geminiApiKey, String geminiModel,
                            GeminiAiCopilotProvider customGemini) {
        this.configuredProvider = configuredProvider != null ? configuredProvider : "local";
        this.geminiApiKey = geminiApiKey;
        this.geminiModel = geminiModel;
        this.ruleEngineProvider = new RuleEngineCopilotProvider();
        if (customGemini != null) {
            this.geminiProvider = customGemini;
        } else {
            this.geminiProvider = new GeminiAiCopilotProvider(geminiApiKey, geminiModel, 12, ruleEngineProvider, null);
        }
        applyProviderSelection();
    }

    private static String resolveConfiguredProvider() {
        String p = System.getProperty("copilot.provider");
        if (p == null || p.isBlank()) {
            p = System.getenv("COPILOT_PROVIDER");
        }
        return (p != null && !p.isBlank()) ? p.trim().toLowerCase() : "local";
    }

    @PostConstruct
    public void init() {
        initProvider();
        if (this.geminiProvider != null && this.geminiProvider.isAvailable() && "gemini".equalsIgnoreCase(configuredProvider)) {
            this.geminiProvider.runStartupConnectivityTest();
        }
    }

    public synchronized void initProvider() {
        if (this.ruleEngineProvider == null) {
            this.ruleEngineProvider = new RuleEngineCopilotProvider();
        }
        this.geminiProvider = new GeminiAiCopilotProvider(geminiApiKey, geminiModel, geminiTimeoutSeconds, ruleEngineProvider, null);
        this.groqProvider = new GroqAiCopilotProvider(groqApiKey, groqModel, 10, ruleEngineProvider, null);
        this.openRouterProvider = new OpenRouterAiCopilotProvider(openrouterApiKey, openrouterModel, 12, ruleEngineProvider, null);
        applyProviderSelection();
    }

    private void applyProviderSelection() {
        String choice = configuredProvider != null ? configuredProvider.trim().toLowerCase() : "local";
        switch (choice) {
            case "groq" -> {
                if (groqProvider != null && groqProvider.isAvailable()) {
                    this.activeProvider = groqProvider;
                    LOGGER.log(Level.INFO, "ERRORCab Copilot initialized with Groq Cloud Provider ({0}).", groqModel);
                } else {
                    LOGGER.log(Level.INFO, "Provider 'groq' selected but GROQ_API_KEY is not set. Falling back to Local Rule Engine.");
                    this.activeProvider = ruleEngineProvider;
                }
            }
            case "openrouter" -> {
                if (openRouterProvider != null && openRouterProvider.isAvailable()) {
                    this.activeProvider = openRouterProvider;
                    LOGGER.log(Level.INFO, "ERRORCab Copilot initialized with OpenRouter Provider ({0}).", openrouterModel);
                } else {
                    LOGGER.log(Level.INFO, "Provider 'openrouter' selected but OPENROUTER_API_KEY is not set. Falling back to Local Rule Engine.");
                    this.activeProvider = ruleEngineProvider;
                }
            }
            case "gemini" -> {
                if (geminiProvider != null && geminiProvider.isAvailable()) {
                    this.activeProvider = geminiProvider;
                    LOGGER.log(Level.INFO, "ERRORCab Copilot initialized with Google Gemini Provider ({0}).", geminiModel);
                } else {
                    LOGGER.log(Level.INFO, "Provider 'gemini' selected but GEMINI_API_KEY is not set. Falling back to Local Rule Engine.");
                    this.activeProvider = ruleEngineProvider;
                }
            }
            default -> {
                this.activeProvider = ruleEngineProvider;
                LOGGER.log(Level.INFO, "ERRORCab Copilot initialized with Local Rule Engine (Offline Foundation).");
            }
        }
    }

    /**
     * Swaps or configures the active AI Provider directly.
     */
    public void setActiveProvider(AiCopilotProvider provider) {
        if (provider != null) {
            this.activeProvider = provider;
        }
    }

    public AiCopilotProvider getActiveProvider() {
        return activeProvider;
    }

    public RuleEngineCopilotProvider getRuleEngineProvider() {
        return ruleEngineProvider;
    }

    public GeminiAiCopilotProvider getGeminiProvider() {
        return geminiProvider;
    }

    public GroqAiCopilotProvider getGroqProvider() {
        return groqProvider;
    }

    public OpenRouterAiCopilotProvider getOpenRouterProvider() {
        return openRouterProvider;
    }

    public String getConfiguredProvider() {
        return configuredProvider;
    }

    public void setConfiguredProvider(String configuredProvider) {
        this.configuredProvider = configuredProvider;
        applyProviderSelection();
    }

    public void setGeminiApiKey(String key) {
        this.geminiApiKey = key;
        initProvider();
    }

    public void setGroqApiKey(String key) {
        this.groqApiKey = key;
        initProvider();
    }

    public void setOpenrouterApiKey(String key) {
        this.openrouterApiKey = key;
        initProvider();
    }

    /**
     * Gathers actual existing ERRORCab context for the specified passenger ID.
     * If passengerId is not found, attempts lookup of the default demo passenger.
     */
    public CopilotContext buildContext(int passengerId) {
        User user = null;
        if (passengerId > 0) {
            Optional<User> userOpt = userRepository.findById(passengerId);
            if (userOpt.isPresent()) {
                user = userOpt.get();
            }
        }

        // If user not found by ID, fall back to demo passenger
        if (user == null) {
            Optional<User> demoOpt = userRepository.findByEmail(AppConfig.DEMO_PASSENGER_EMAIL);
            if (demoOpt.isPresent()) {
                user = demoOpt.get();
                passengerId = user.getId();
            }
        }

        int targetId = user != null ? user.getId() : passengerId;
        List<FavoriteLocation> favorites = favoriteRepository.getFavoritesByPassenger(targetId);
        List<Booking> recentTrips = bookingRepository.getBookingsByPassenger(targetId);
        Booking activeBooking = bookingRepository.findActiveBookingForPassenger(targetId).orElse(null);

        List<Driver> allDrivers = driverRepository.getAllDrivers();
        int onlineCount = (int) allDrivers.stream().filter(Driver::isOnline).count();

        List<String> cabClasses = Arrays.stream(CabType.values())
                .map(Enum::name)
                .toList();

        return new CopilotContext(targetId, user, favorites, recentTrips, activeBooking, onlineCount, cabClasses);
    }

    /**
     * Generates a personalized travel plan with ERRORCab ride suggestions.
     * If the active provider fails, automatically falls back to RuleEngineCopilotProvider.
     */
    public CopilotResponse generateTravelPlan(CopilotTripRequest request) {
        if (request == null) {
            request = new CopilotTripRequest();
        }

        CopilotContext context = buildContext(request.getPassengerId());
        try {
            return activeProvider.generatePlan(context, request);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Active AI provider execution failed ({0}). Invoking emergency rule-engine fallback.", e.getMessage());
            return ruleEngineProvider.generatePlan(context, request);
        }
    }

    /**
     * Health and status report of the AI Copilot subsystem.
     */
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("feature", "ERRORCab AI — Travel Copilot");
        status.put("version", "3.0.0-multi-provider");
        status.put("configuredProvider", configuredProvider);
        status.put("activeProvider", activeProvider != null ? activeProvider.getProviderName() : "None");
        status.put("fallbackProvider", ruleEngineProvider.getProviderName());
        status.put("supportedProviders", List.of("local", "openrouter", "groq", "gemini"));
        status.put("geminiAvailable", geminiProvider != null && geminiProvider.isAvailable());
        status.put("groqAvailable", groqProvider != null && groqProvider.isAvailable());
        status.put("openrouterAvailable", openRouterProvider != null && openRouterProvider.isAvailable());
        status.put("geminiModel", geminiModel);
        status.put("fallbackAvailable", true);
        status.put("externalAiReady", true);

        String mode = "OFFLINE_RULE_ENGINE";
        if (activeProvider instanceof GroqAiCopilotProvider && activeProvider.isAvailable()) {
            mode = "ONLINE_GROQ";
        } else if (activeProvider instanceof OpenRouterAiCopilotProvider && activeProvider.isAvailable()) {
            mode = "ONLINE_OPENROUTER";
        } else if (activeProvider instanceof GeminiAiCopilotProvider && activeProvider.isAvailable()) {
            mode = "ONLINE_GEMINI";
        }
        status.put("mode", mode);
        status.put("destinationKnowledgeBaseSize", DestinationKnowledgeBase.getAll().size());
        status.put("connectedDataSources", List.of(
                "destination_intelligence_layer",
                "passenger_profile",
                "favorite_locations",
                "ride_history",
                "active_booking",
                "fleet_status"
        ));
        return status;
    }

    /**
     * Returns schema and presets for travel preferences to support frontend UI.
     */
    public Map<String, Object> getPreferencesSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();

        schema.put("purposes", List.of(
                "Leisure & Tourism",
                "Weekend Getaway",
                "Culinary & Food Tour",
                "Business & Work",
                "Family Outing",
                "Culture & Heritage"
        ));

        schema.put("popularDestinations", List.of(
                "Fort Kochi",
                "Kozhikode",
                "Wayanad",
                "Vagamon",
                "Munnar",
                "Alappuzha",
                "Varkala",
                "Kovalam",
                "Bekal",
                "Kannur",
                "Thrissur",
                "Kumarakom",
                "Thekkady",
                "Idukki",
                "Kollam",
                "Thiruvananthapuram",
                "Edappally",
                "Kakkanad",
                "Goa"
        ));

        schema.put("durations", List.of(
                "Half-day (4-5 hrs)",
                "Full-day (8-10 hrs)",
                "Weekend (2 days)",
                "3+ days Extended"
        ));

        schema.put("budgets", List.of(
                "Budget (₹500 - ₹1,500)",
                "Moderate (₹1,500 - ₹3,500)",
                "Premium (₹3,500+)"
        ));

        schema.put("interests", List.of(
                "Heritage & History",
                "Scenic Nature & Waterways",
                "Beaches & Sunset",
                "Shopping & Retail",
                "Tech Hubs & Workspaces",
                "Architecture & Photography"
        ));

        schema.put("foodPreferences", List.of(
                "Kerala Traditional & Sadhya",
                "Fresh Coastal Seafood",
                "Malabar Biryani & Halwa",
                "Artisanal Cafes & Bakeries",
                "Pure Vegetarian Delights",
                "Late-night Street Food & Dosas"
        ));

        schema.put("activityPreferences", List.of(
                "Sightseeing & Landmarks",
                "Relaxed & Leisurely Pace",
                "Walking & Promenade Tour",
                "Photography & Viewpoints",
                "Water Metro & Boating"
        ));

        return schema;
    }
}
