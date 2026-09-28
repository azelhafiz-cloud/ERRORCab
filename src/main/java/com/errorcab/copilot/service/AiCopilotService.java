package com.errorcab.copilot.service;

import com.errorcab.config.AppConfig;
import com.errorcab.copilot.model.CopilotContext;
import com.errorcab.copilot.model.CopilotResponse;
import com.errorcab.copilot.model.CopilotTripRequest;
import com.errorcab.copilot.provider.AiCopilotProvider;
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
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service orchestrating ERRORCab AI — Travel Copilot.
 * Synthesizes actual existing ERRORCab context (profile, favorites, past trips, active rides, fleet)
 * and delegates plan synthesis to a pluggable AiCopilotProvider.
 */
@Service
public class AiCopilotService {

    private final UserRepository userRepository = new UserRepository();
    private final FavoriteRepository favoriteRepository = new FavoriteRepository();
    private final BookingRepository bookingRepository = new BookingRepository();
    private final DriverRepository driverRepository = new DriverRepository();

    // Active AI Provider (currently local offline rule-engine; ready for external provider injection)
    private AiCopilotProvider activeProvider = new RuleEngineCopilotProvider();

    public AiCopilotService() {}

    public AiCopilotService(AiCopilotProvider provider) {
        if (provider != null) {
            this.activeProvider = provider;
        }
    }

    /**
     * Swaps or configures the active AI Provider (for future external API integration in Stage 2).
     */
    public void setActiveProvider(AiCopilotProvider provider) {
        if (provider != null) {
            this.activeProvider = provider;
        }
    }

    public AiCopilotProvider getActiveProvider() {
        return activeProvider;
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
     */
    public CopilotResponse generateTravelPlan(CopilotTripRequest request) {
        if (request == null) {
            request = new CopilotTripRequest();
        }

        CopilotContext context = buildContext(request.getPassengerId());
        return activeProvider.generatePlan(context, request);
    }

    /**
     * Health and status report of the AI Copilot subsystem.
     */
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("feature", "ERRORCab AI — Travel Copilot");
        status.put("version", "1.0.0-foundation");
        status.put("provider", activeProvider.getProviderName());
        status.put("available", activeProvider.isAvailable());
        status.put("mode", "OFFLINE_LOCAL_ENGINE");
        status.put("externalAiReady", true);
        status.put("connectedDataSources", List.of(
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
                "Edappally",
                "Kakkanad",
                "Vyttila",
                "Thrippunithura",
                "MG Road",
                "Aluva",
                "Munnar",
                "Alappuzha"
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
