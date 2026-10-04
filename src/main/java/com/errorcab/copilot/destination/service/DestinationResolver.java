package com.errorcab.copilot.destination.service;

import com.errorcab.copilot.destination.model.DestinationProfile;
import com.errorcab.copilot.destination.model.DestinationResult;
import com.errorcab.copilot.destination.model.SafetyAdvisory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resolves arbitrary Indian destinations through a modular intelligence pipeline:
 * 1. In-memory resolution cache
 * 2. Verified Knowledge Base (Instant, authentic, zero-network)
 * 3. Geocoding Provider (OpenStreetMap Nominatim for India)
 * 4. Public factual discovery (Wikipedia REST summary API)
 * 5. Strict unresolved handling ("Couldn't confidently locate this destination")
 * Never silently uses a random fallback destination.
 */
public class DestinationResolver {

    private static final Logger LOGGER = Logger.getLogger(DestinationResolver.class.getName());
    private static final Map<String, DestinationResult> RESULT_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, DestinationProfile> PROFILE_CACHE = new ConcurrentHashMap<>();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(2500))
            .build();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final GeocodingProvider geocodingProvider;
    private final boolean allowPublicDiscovery;

    public DestinationResolver(GeocodingProvider geocodingProvider, boolean allowPublicDiscovery) {
        this.geocodingProvider = geocodingProvider != null ? geocodingProvider : new NominatimGeocodingProvider();
        this.allowPublicDiscovery = allowPublicDiscovery;
    }

    public DestinationResolver(boolean allowPublicDiscovery) {
        this(new NominatimGeocodingProvider(), allowPublicDiscovery);
    }

    public DestinationResolver() {
        this(new NominatimGeocodingProvider(), true);
    }

    /**
     * Resolves a destination into a structured DestinationResult containing
     * coordinates, administrative entity (district/state), and resolution status.
     */
    public DestinationResult resolveDestination(String destinationInput) {
        if (destinationInput == null || destinationInput.trim().isEmpty()) {
            DestinationProfile kochi = DestinationKnowledgeBase.find("kochi");
            if (kochi != null) {
                return new DestinationResult(
                        "Fort Kochi", kochi.getDestinationName(), kochi.getDistrict(),
                        kochi.getState(), kochi.getCountry(), kochi.getLatitude(), kochi.getLongitude(),
                        kochi.getDestinationName() + ", " + kochi.getState(), true, "KNOWLEDGE_BASE"
                );
            }
            return new DestinationResult("Fort Kochi", "Fort Kochi", "Ernakulam", "Kerala", "India",
                    9.9658, 76.2421, "Fort Kochi, Kerala", true, "KNOWLEDGE_BASE");
        }

        String raw = destinationInput.trim();
        String normalizedKey = normalize(raw);

        // 1. Check in-memory resolution cache
        if (RESULT_CACHE.containsKey(normalizedKey)) {
            return RESULT_CACHE.get(normalizedKey);
        }

        // 2. Check verified local knowledge base
        DestinationProfile verified = DestinationKnowledgeBase.find(raw);
        if (verified != null) {
            DestinationResult result = new DestinationResult(
                    raw,
                    verified.getDestinationName(),
                    verified.getDistrict(),
                    verified.getState(),
                    verified.getCountry(),
                    verified.getLatitude(),
                    verified.getLongitude(),
                    verified.getDestinationName() + ", " + verified.getState(),
                    true,
                    "KNOWLEDGE_BASE"
            );
            RESULT_CACHE.put(normalizedKey, result);
            return result;
        }

        // 3. Attempt geocoding via GeocodingProvider (e.g. OpenStreetMap Nominatim)
        if (geocodingProvider != null && geocodingProvider.isAvailable()) {
            try {
                DestinationResult geoResult = geocodingProvider.geocode(raw);
                if (geoResult != null && geoResult.isResolved()) {
                    RESULT_CACHE.put(normalizedKey, geoResult);
                    return geoResult;
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Geocoding error for {0}: {1}", new Object[]{raw, e.getMessage()});
            }
        }

        // 4. Attempt public discovery via Wikipedia summary API
        if (allowPublicDiscovery) {
            try {
                DestinationResult wikiResult = discoverResultFromWikipedia(raw);
                if (wikiResult != null && wikiResult.isResolved()) {
                    RESULT_CACHE.put(normalizedKey, wikiResult);
                    return wikiResult;
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Wikipedia discovery failed for {0}: {1}", new Object[]{raw, e.getMessage()});
            }
        }

        // 5. Unresolved: Never silently pick a random fallback destination!
        DestinationResult unresolved = DestinationResult.unresolved(raw);
        RESULT_CACHE.put(normalizedKey, unresolved);
        return unresolved;
    }

    /**
     * Resolves a destination into a DestinationProfile.
     * If unresolved, returns DestinationProfile.createUnresolved(raw) with
     * "Couldn't confidently locate this destination. Try adding the district or state."
     */
    public DestinationProfile resolve(String destinationInput) {
        DestinationResult result = resolveDestination(destinationInput);

        if (!result.isResolved()) {
            return DestinationProfile.createUnresolved(result.getQuery());
        }

        String normalizedKey = normalize(result.getNormalizedPlaceName());
        if (PROFILE_CACHE.containsKey(normalizedKey)) {
            return PROFILE_CACHE.get(normalizedKey);
        }

        // Check if verified profile exists in knowledge base
        DestinationProfile verified = DestinationKnowledgeBase.find(result.getNormalizedPlaceName());
        if (verified != null) {
            PROFILE_CACHE.put(normalizedKey, verified);
            return verified;
        }

        // Construct dynamically discovered profile from DestinationResult and Wikipedia facts
        DestinationProfile profile = buildDiscoveredProfile(result);
        PROFILE_CACHE.put(normalizedKey, profile);
        return profile;
    }

    private DestinationResult discoverResultFromWikipedia(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://en.wikipedia.org/api/rest_v1/page/summary/" + encoded;

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "ERRORCab-India-Travel-Copilot/3.0 (discovery@errorcab.com)")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofMillis(2500))
                    .GET()
                    .build();

            HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && resp.body() != null && !resp.body().isBlank()) {
                JsonNode root = OBJECT_MAPPER.readTree(resp.body());
                String title = root.path("title").asText(query);
                String extract = root.path("extract").asText("");
                String description = root.path("description").asText("Geographical destination");

                if (!extract.isBlank() && !root.path("type").asText("").equalsIgnoreCase("disambiguation")) {
                    double lat = 0.0;
                    double lon = 0.0;
                    JsonNode coordinates = root.path("coordinates");
                    if (!coordinates.isMissingNode()) {
                        lat = coordinates.path("lat").asDouble(0.0);
                        lon = coordinates.path("lon").asDouble(0.0);
                    }

                    DestinationResult res = new DestinationResult(
                            query,
                            title,
                            null,
                            "India",
                            "India",
                            lat,
                            lon,
                            title + ", India",
                            true,
                            "PUBLIC_DISCOVERY"
                    );
                    res.getMetadata().put("extract", extract);
                    res.getMetadata().put("description", description);
                    return res;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Wikipedia request error: {0}", e.getMessage());
        }
        return null;
    }

    private DestinationProfile buildDiscoveredProfile(DestinationResult result) {
        DestinationProfile profile = new DestinationProfile();
        String place = result.getNormalizedPlaceName();
        profile.setDestinationName(place);
        profile.setDistrict(result.getDistrict());
        profile.setState(result.getState() != null ? result.getState() : "India");
        profile.setCountry(result.getCountry() != null ? result.getCountry() : "India");
        profile.setLatitude(result.getLatitude());
        profile.setLongitude(result.getLongitude());
        profile.setRegion(result.getState());
        profile.setDestinationType("Indian Destination");
        profile.setKnowledgeStatus("DISCOVERED_PUBLIC_DATA");
        profile.setSourceInformation("Source: " + result.getResolutionSource() + " & Open Knowledge Base");

        String extract = (String) result.getMetadata().get("extract");
        if (extract != null && !extract.isBlank()) {
            profile.setShortDescription(extract);
            List<String> sentences = extractSentences(extract, 3);
            profile.setMajorHighlights(sentences);
            profile.setBestKnownFor((String) result.getMetadata().getOrDefault("description", place + " Heritage & Sights"));
        } else {
            profile.setShortDescription("Authentic travel exploration of " + place + " in " + profile.getState() + ", India.");
            profile.setMajorHighlights(List.of(
                    place + " Central Historical Quarter",
                    place + " Prominent Public Promenade",
                    place + " Regional Cultural Landmarks"
            ));
            profile.setBestKnownFor("Regional landmarks and cultural attractions in " + place);
        }

        profile.setAttractions(List.of(place + " Main Square", place + " Heritage Landmark", place + " Scenic Promenade"));
        profile.setSuggestedActivities(List.of(
                "Exploring prominent landmarks of " + place,
                "Cultural and historic walking exploration",
                "Sampling authentic regional cuisine"
        ));
        profile.setCulinaryHighlights(List.of(
                "Authentic regional specialties of " + profile.getState(),
                "Traditional market street food stalls",
                "Freshly brewed local tea and coffee"
        ));
        profile.setLocalSpecialities(List.of(
                place + " Historic Architecture",
                place + " Local Handicrafts & Textiles",
                place + " Traditional Cuisine"
        ));
        profile.setLocalTravelAdvice(List.of(
                "Check operational timings for prominent heritage monuments prior to arrival.",
                "Carry modest attire suitable for local cultural and religious places.",
                "Book direct ERRORCab transit for predictable, transparent travel."
        ));
        profile.setTransportAdvice("Book ERRORCab verified private cab transit with upfront zero-surge pricing.");
        profile.setTypicalTripDuration("Half-day (4-5 hrs) to Full-day (8 hrs)");
        profile.setFamilySuitability("Suitable for travel exploration.");
        profile.setBudgetNotes("Standard regional travel budget.");

        // Strict safety rule: NEVER invent fake warnings!
        profile.getSafetyNotes().add(SafetyAdvisory.noVerifiedAdvisoryFound());

        return profile;
    }

    private List<String> extractSentences(String text, int max) {
        List<String> list = new ArrayList<>();
        if (text == null || text.isBlank()) return list;
        String[] parts = text.split("\\.\\s+");
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                if (!trimmed.endsWith(".")) trimmed += ".";
                list.add(trimmed);
                if (list.size() >= max) break;
            }
        }
        return list;
    }

    public static void clearCache() {
        RESULT_CACHE.clear();
        PROFILE_CACHE.clear();
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "")
                .trim();
    }
}
