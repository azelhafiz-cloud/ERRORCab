package com.errorcab.copilot.provider;

import com.errorcab.copilot.model.CopilotContext;
import com.errorcab.copilot.model.CopilotResponse;
import com.errorcab.copilot.model.CopilotRideSuggestion;
import com.errorcab.copilot.model.CopilotTripRequest;
import com.errorcab.copilot.model.ItineraryLeg;
import com.errorcab.model.CabType;
import com.errorcab.service.FareService;
import com.errorcab.service.MapService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * High-performance, offline rule-based AI provider for ERRORCab.
 * Synthesizes personalized travel itineraries and ERRORCab ride suggestions
 * tailored to Kerala / Kochi landmarks, actual distances, and polymorphic fare structures.
 * Operates 100% offline without external API keys.
 */
public class RuleEngineCopilotProvider implements AiCopilotProvider {

    private final MapService mapService = MapService.getInstance();
    private final FareService fareService = FareService.getInstance();

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
        CopilotResponse response = new CopilotResponse();
        response.setProviderName(getProviderName());
        response.setTripPurpose(request.getTripPurpose() != null ? request.getTripPurpose() : "Leisure & Tourism");
        response.setDestination(request.getDestination() != null ? request.getDestination() : "Fort Kochi");
        response.setDuration(request.getDuration() != null ? request.getDuration() : "Half-day (4-5 hrs)");
        response.setBudget(request.getBudget() != null ? request.getBudget() : "Moderate (₹1,500 - ₹3,500)");

        String passengerName = context != null && context.getPassengerName() != null ? context.getPassengerName() : "Traveler";
        String pickupHub = context != null && context.getResolvedPickupLocation() != null ? context.getResolvedPickupLocation() : "Kakkanad";
        String destination = response.getDestination();

        // Determine recommended cab type based on user budget preference
        CabType recommendedCab = resolveCabType(request.getBudget());

        response.setTitle("Personalized " + destination + " " + response.getTripPurpose() + " Itinerary");
        response.setSummary(buildSummary(passengerName, pickupHub, destination, response.getDuration(), response.getBudget(), request));

        // 1. Generate Itinerary Legs based on destination and preferences
        List<ItineraryLeg> legs = buildItineraryLegs(pickupHub, destination, recommendedCab, request);
        response.setItinerary(legs);

        // 2. Extract suggested ERRORCab rides from the itinerary
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

        // 3. Compile Curated Food Recommendations
        response.setFoodRecommendations(buildFoodRecommendations(destination, request.getFoodPreferences()));

        // 4. Compile Local Tips & Safety Advice
        response.setTravelTips(buildTravelTips(destination, response.getTripPurpose(), request.getInterests()));

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

    private String buildSummary(String passengerName, String pickupHub, String destination,
                                 String duration, String budget, CopilotTripRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Welcome, ").append(passengerName).append("! ");
        sb.append("Here is your curated ").append(duration.toLowerCase()).append(" travel plan for ")
          .append(destination).append(", starting comfortably from your home hub at ").append(pickupHub).append(". ");

        if (request.getInterests() != null && !request.getInterests().isEmpty()) {
            sb.append("Customized around your interests in ")
              .append(String.join(", ", request.getInterests())).append(". ");
        }

        sb.append("All connecting transit is integrated with ERRORCab ").append(resolveCabType(budget).name())
          .append(" rides for guaranteed transparent pricing and verified Kerala drivers.");

        return sb.toString();
    }

    private List<ItineraryLeg> buildItineraryLegs(String pickupHub, String dest, CabType cabType, CopilotTripRequest req) {
        List<ItineraryLeg> list = new ArrayList<>();
        String d = dest.toLowerCase();

        // Calculate outward cab ride from pickup to destination
        double outwardDist = mapService.getDistanceKm(pickupHub, dest);
        int outwardMins = mapService.getEstimatedMinutes(outwardDist);
        double outwardFare = fareService.calculateFare(cabType, outwardDist);

        CopilotRideSuggestion outwardRide = new CopilotRideSuggestion(
                "Leg 1 Ride: " + pickupHub + " ➔ " + dest,
                pickupHub, dest, outwardDist, outwardMins, cabType, outwardFare,
                "Prompt dispatch from " + pickupHub + " directly to " + dest + " hub."
        );

        if (d.contains("fort kochi") || d.contains("kochi")) {
            list.add(new ItineraryLeg(
                    "09:00 AM - 10:15 AM",
                    "Morning Dispatch & Transit to Heritage Zone",
                    pickupHub + " to Fort Kochi",
                    "Transit",
                    "Sit back in your ERRORCab " + cabType + " cab as you head towards the historic port district.",
                    outwardRide
            ));

            list.add(new ItineraryLeg(
                    "10:15 AM - 12:00 PM",
                    "Chinese Fishing Nets & Coastal Promenade",
                    "Fort Kochi Beach Walkway",
                    "Sightseeing",
                    "Explore the 14th-century cantilevered fishing nets and Vasco da Gama Square. Ideal for scenic coastal photography and gentle sea breezes.",
                    null
            ));

            list.add(new ItineraryLeg(
                    "12:15 PM - 02:00 PM",
                    "Jew Town, Paradesi Synagogue & Artisanal Lunch",
                    "Mattancherry & Jew Town",
                    "Culture & Dining",
                    "Wander through antique shops, spice warehouses, and enjoy lunch at a heritage cafe like Kashi Art Cafe or Seagull Waterfront.",
                    null
            ));

            list.add(new ItineraryLeg(
                    "02:30 PM - 04:30 PM",
                    "Indo-Portuguese Museum & Santa Cruz Basilica",
                    "Fort Kochi Cultural Square",
                    "Heritage",
                    "Admire Gothic architecture, woodcarvings, and local art exhibitions before wrapping up your heritage tour.",
                    null
            ));

            // Return leg
            double returnDist = outwardDist;
            int returnMins = outwardMins;
            double returnFare = outwardFare;
            CopilotRideSuggestion returnRide = new CopilotRideSuggestion(
                    "Return Ride: Fort Kochi ➔ " + pickupHub,
                    "Fort Kochi", pickupHub, returnDist, returnMins, cabType, returnFare,
                    "Comfortable AC ride back home to " + pickupHub + " avoiding peak evening bottlenecks."
            );

            list.add(new ItineraryLeg(
                    "05:00 PM - 06:15 PM",
                    "Evening Return Ride to " + pickupHub,
                    "Fort Kochi to " + pickupHub,
                    "Transit",
                    "Catch your pre-booked ERRORCab ride for a smooth return journey home.",
                    returnRide
            ));

        } else if (d.contains("edappally") || d.contains("lulu")) {
            list.add(new ItineraryLeg(
                    "10:00 AM - 10:45 AM",
                    "Morning Cab to Edappally Commercial Center",
                    pickupHub + " to Edappally",
                    "Transit",
                    "Direct ride via bypass corridor directly to Edappally Junction.",
                    outwardRide
            ));

            list.add(new ItineraryLeg(
                    "11:00 AM - 01:30 PM",
                    "LuLu International Shopping & Entertainment",
                    "Edappally Toll",
                    "Shopping & Leisure",
                    "Experience Asia's premier retail destination with multiplexes, international stores, and indoor ice skating.",
                    null
            ));

            list.add(new ItineraryLeg(
                    "01:30 PM - 03:00 PM",
                    "Malabar Culinary Lunch at Paragon",
                    "Edappally High Street",
                    "Dining",
                    "Savor authentic Kozhikode biryani, soft appams, and seafood specialties.",
                    null
            ));

            list.add(new ItineraryLeg(
                    "03:15 PM - 05:00 PM",
                    "Museum of Kerala History & St. George Forane Church",
                    "Edappally Heritage Zone",
                    "Culture",
                    "Explore life-size historical dioramas and the architectural grandeur of one of India's oldest churches.",
                    null
            ));

            CopilotRideSuggestion returnRide = new CopilotRideSuggestion(
                    "Return Ride: Edappally ➔ " + pickupHub,
                    "Edappally", pickupHub, outwardDist, outwardMins, cabType, outwardFare,
                    "Quick return transfer to " + pickupHub + "."
            );
            list.add(new ItineraryLeg(
                    "05:30 PM",
                    "Convenient Evening Return Ride",
                    "Edappally to " + pickupHub,
                    "Transit",
                    "Head home safely with zero surge fares on ERRORCab.",
                    returnRide
            ));

        } else if (d.contains("kakkanad") || d.contains("infopark")) {
            list.add(new ItineraryLeg(
                    "09:30 AM - 10:15 AM",
                    "Express Ride to Kakkanad IT Corridor",
                    pickupHub + " to Kakkanad",
                    "Transit",
                    "Smooth highway transit to Infopark and SmartCity campus.",
                    outwardRide
            ));

            list.add(new ItineraryLeg(
                    "10:30 AM - 01:00 PM",
                    "Tech Hub Meetings & Coworking Stint",
                    "Infopark Phase 1 & 2",
                    "Business",
                    "Network, attend client meetings, or work productively at Infopark's premium tech zones.",
                    null
            ));

            list.add(new ItineraryLeg(
                    "01:15 PM - 02:30 PM",
                    "Corporate Lunch & Specialty Coffee",
                    "Kakkanad Central",
                    "Dining",
                    "Enjoy contemporary dining and artisanal coffees at Pandhal or local Kerala Thali spots.",
                    null
            ));

            list.add(new ItineraryLeg(
                    "03:00 PM - 05:00 PM",
                    "Kadambrayar Eco-Tourism Waterfront & Sunset",
                    "Kadambrayar Eco Park",
                    "Nature",
                    "Relax along the tranquil riverbank with pedal boating and lush green coconut groves.",
                    null
            ));

        } else if (d.contains("vyttila") || d.contains("thrippunithura")) {
            list.add(new ItineraryLeg(
                    "09:30 AM - 10:15 AM",
                    "Cab Transfer to Vyttila Hub",
                    pickupHub + " to Vyttila",
                    "Transit",
                    "Fast connection to South India's largest integrated transit hub.",
                    outwardRide
            ));

            list.add(new ItineraryLeg(
                    "10:30 AM - 12:30 PM",
                    "Kochi Water Metro Experience",
                    "Vyttila Water Metro Terminal",
                    "Activity & Transit",
                    "Board the sleek electric hybrid AC water metro for a serene cruise across the backwater channels.",
                    null
            ));

            list.add(new ItineraryLeg(
                    "01:00 PM - 03:30 PM",
                    "Hill Palace Archaeological Museum",
                    "Thrippunithura",
                    "Heritage & History",
                    "Tour the 54-acre royal palace complex of the Kochi Maharajas, housing gold crowns, royal carriages, and botanical gardens.",
                    null
            ));

            CopilotRideSuggestion returnRide = new CopilotRideSuggestion(
                    "Return Ride: Thrippunithura ➔ " + pickupHub,
                    "Thrippunithura", pickupHub, outwardDist, outwardMins, cabType, outwardFare,
                    "Direct pickup from Hill Palace gateway back to " + pickupHub + "."
            );
            list.add(new ItineraryLeg(
                    "04:30 PM",
                    "Return Trip to " + pickupHub,
                    "Thrippunithura to " + pickupHub,
                    "Transit",
                    "Prompt driver arrival at Hill Palace gates.",
                    returnRide
            ));

        } else if (d.contains("munnar") || d.contains("alappuzha") || d.contains("trivandrum")) {
            // Extended Kerala destination
            list.add(new ItineraryLeg(
                    "Day 1: 07:00 AM - 10:30 AM",
                    "Early Morning Scenic ERRORCab Transfer",
                    pickupHub + " to " + dest,
                    "Transit",
                    "Comfortable long-distance " + cabType + " journey through lush Kerala valleys with experienced highway drivers.",
                    outwardRide
            ));

            list.add(new ItineraryLeg(
                    "Day 1: 11:30 AM - 03:00 PM",
                    dest + " Signature Attractions & Cultural Discovery",
                    dest + " Central",
                    "Sightseeing",
                    "Immerse yourself in scenic sights, local viewpoints, and traditional Kerala hospitality.",
                    null
            ));

            list.add(new ItineraryLeg(
                    "Day 1: Evening - Day 2",
                    "Sunset Leisure & Traditional Kerala Dining",
                    dest + " Promenade",
                    "Dining & Relaxation",
                    "Indulge in regional delicacies and serene nature trails.",
                    null
            ));

        } else {
            // Generalized destination
            list.add(new ItineraryLeg(
                    "09:30 AM - 10:30 AM",
                    "ERRORCab Transfer from " + pickupHub + " to " + dest,
                    pickupHub + " to " + dest,
                    "Transit",
                    "Direct, doorstep cab dispatch with zero surge pricing.",
                    outwardRide
            ));

            list.add(new ItineraryLeg(
                    "10:45 AM - 01:00 PM",
                    dest + " Core Sights & Activities",
                    dest,
                    "Sightseeing",
                    "Explore primary landmarks, markets, and regional highlights aligned with your preferences.",
                    null
            ));

            list.add(new ItineraryLeg(
                    "01:15 PM - 02:45 PM",
                    "Authentic Local Dining & Relaxation",
                    dest + " Central",
                    "Dining",
                    "Curated regional lunch tailored to your dietary choices.",
                    null
            ));

            list.add(new ItineraryLeg(
                    "03:00 PM - 05:00 PM",
                    "Leisure Stroll & Photo Opportunities",
                    dest + " Viewpoint",
                    "Leisure",
                    "Enjoy the afternoon ambiance before returning.",
                    null
            ));

            CopilotRideSuggestion returnRide = new CopilotRideSuggestion(
                    "Return Ride: " + dest + " ➔ " + pickupHub,
                    dest, pickupHub, outwardDist, outwardMins, cabType, outwardFare,
                    "Safe and verified evening cab ride back to " + pickupHub + "."
            );
            list.add(new ItineraryLeg(
                    "05:30 PM",
                    "Comfortable Return Journey to " + pickupHub,
                    dest + " to " + pickupHub,
                    "Transit",
                    "Your verified ERRORCab driver picks you up right at your destination.",
                    returnRide
            ));
        }

        return list;
    }

    private List<String> buildFoodRecommendations(String destination, List<String> preferences) {
        List<String> foods = new ArrayList<>();
        boolean wantsVeg = preferences != null && preferences.stream().anyMatch(p -> p.toLowerCase().contains("veg"));
        boolean wantsSeafood = preferences != null && preferences.stream().anyMatch(p -> p.toLowerCase().contains("sea"));
        boolean wantsCafe = preferences != null && preferences.stream().anyMatch(p -> p.toLowerCase().contains("cafe"));

        if (wantsVeg) {
            foods.add("Brindhavan Vegetarian (Palarivattom) - Famous for Crispy Podi Ghee Roast, Mysore Dosa & Filter Coffee.");
            foods.add("Gokul Oottupura (MG Road / Kakkanad) - Pure traditional Kerala banana-leaf Sadya with avial, payasam, and sambar.");
        } else if (wantsSeafood) {
            foods.add("Seagull Waterfront (Fort Kochi) - Fresh catch Karimeen Pollichathu, Tiger Prawns Fry & sea breeze.");
            foods.add("Grand Hotel Restaurant (MG Road) - Legendary Kerala Fish Curry meals with seer fish and kappa.");
        }

        if (wantsCafe) {
            foods.add("Kashi Art Cafe (Burgher St, Fort Kochi) - Renowned chocolate cake, cold-brew coffees, and courtyard art installations.");
            foods.add("Pandhal Cafe & Deli (MG Road / Kakkanad) - Artisanal sourdough, brioche toast, and specialty Kerala pour-overs.");
        }

        // Default local signatures
        if (foods.isEmpty()) {
            foods.add("Paragon Restaurant (Lulu Mall & Calicut style) - Award-winning Mutton & Chicken Dum Biryani with Malabar parottas.");
            foods.add("Pai Brothers Fast Food (MG Road / Kaloor) - Famous street eatery offering over 36 varieties of fresh dosas.");
            foods.add("Kayees Rahmathulla Hotel (Mattancherry) - Historic Dum Biryani served with date pickle and mint raita.");
        }

        return foods;
    }

    private List<String> buildTravelTips(String destination, String purpose, List<String> interests) {
        List<String> tips = new ArrayList<>();
        tips.add("Direct ERRORCab Booking: Click 'Book this Ride' to pre-fill pickup and dropoff automatically in the booking portal.");
        tips.add("Peak Hour Planning: Kochi traffic on Edappally Bypass and Vyttila peaks between 08:30-10:00 AM and 05:30-07:30 PM. Plan departure 20 minutes earlier.");
        tips.add("Digital Payments: All ERRORCab rides support instant UPI (GPay, PhonePe, Paytm), Card, or Cash upon completion.");
        tips.add("Safety & Tracking: Share your live ride tracking link with friends and family directly from the active trip screen.");
        tips.add("Climate & Attire: Kerala coastal weather is pleasantly humid. Breathable cotton attire and comfortable walking footwear are highly recommended.");
        return tips;
    }
}
