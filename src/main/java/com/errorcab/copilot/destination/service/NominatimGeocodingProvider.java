package com.errorcab.copilot.destination.service;

import com.errorcab.copilot.destination.model.DestinationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Free / open geocoding provider utilizing the OpenStreetMap Nominatim search API.
 * Constrained to India (countrycodes=in) with strict timeouts and error resilience.
 */
public class NominatimGeocodingProvider implements GeocodingProvider {

    private static final Logger LOGGER = Logger.getLogger(NominatimGeocodingProvider.class.getName());
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final Duration TIMEOUT = Duration.ofMillis(2500);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public NominatimGeocodingProvider(HttpClient httpClient) {
        this.httpClient = httpClient != null ? httpClient : HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public NominatimGeocodingProvider() {
        this(null);
    }

    @Override
    public String getProviderName() {
        return "OpenStreetMap Nominatim (India)";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public DestinationResult geocode(String placeName) {
        if (placeName == null || placeName.trim().isEmpty()) {
            return null;
        }

        String query = placeName.trim();
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = NOMINATIM_URL + "?q=" + encoded + "&countrycodes=in&format=json&addressdetails=1&limit=1";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "ERRORCab-India-Travel-Copilot/3.0 (info@errorcab.com)")
                    .header("Accept", "application/json")
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && resp.body() != null && !resp.body().isBlank()) {
                JsonNode array = objectMapper.readTree(resp.body());
                if (array.isArray() && !array.isEmpty()) {
                    JsonNode first = array.get(0);
                    double lat = first.path("lat").asDouble(0.0);
                    double lon = first.path("lon").asDouble(0.0);
                    String displayName = first.path("display_name").asText(query);

                    JsonNode address = first.path("address");
                    String city = address.path("city").asText(
                            address.path("town").asText(
                                    address.path("village").asText(
                                            address.path("suburb").asText(query)
                                    )
                            )
                    );
                    String district = address.path("state_district").asText(
                            address.path("county").asText("")
                    );
                    String state = address.path("state").asText("India");
                    String country = address.path("country").asText("India");

                    DestinationResult result = new DestinationResult(
                            query,
                            capitalize(city.isBlank() ? query : city),
                            district.isBlank() ? null : district,
                            state.isBlank() ? "India" : state,
                            country,
                            lat,
                            lon,
                            displayName,
                            true,
                            "GEOCODER_NOMINATIM"
                    );
                    result.getMetadata().put("osmType", first.path("osm_type").asText(""));
                    result.getMetadata().put("importance", first.path("importance").asDouble(0.0));
                    return result;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Nominatim geocoding failed for {0}: {1}", new Object[]{query, e.getMessage()});
        }
        return null;
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
