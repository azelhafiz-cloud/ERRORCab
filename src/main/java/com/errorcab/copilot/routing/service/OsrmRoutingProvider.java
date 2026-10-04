package com.errorcab.copilot.routing.service;

import com.errorcab.copilot.routing.model.RouteResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Open-source routing provider utilizing OpenStreetMap OSRM public demo routing service.
 */
public class OsrmRoutingProvider implements RoutingProvider {

    private static final Logger LOGGER = Logger.getLogger(OsrmRoutingProvider.class.getName());
    private static final String OSRM_URL_BASE = "http://router.project-osrm.org/route/v1/driving/";
    private static final Duration TIMEOUT = Duration.ofMillis(2500);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OsrmRoutingProvider(HttpClient httpClient) {
        this.httpClient = httpClient != null ? httpClient : HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public OsrmRoutingProvider() {
        this(null);
    }

    @Override
    public String getProviderName() {
        return "OpenStreetMap OSRM Public Routing";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public RouteResult calculateRoute(double startLat, double startLon,
                                       double endLat, double endLon,
                                       String originName, String destName) {
        if (startLat == 0.0 || startLon == 0.0 || endLat == 0.0 || endLon == 0.0) {
            return null;
        }

        try {
            // OSRM expects coordinates in {lon},{lat};{lon},{lat} format
            String coords = String.format(Locale.US, "%.6f,%.6f;%.6f,%.6f", startLon, startLat, endLon, endLat);
            String url = OSRM_URL_BASE + coords + "?overview=false";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "ERRORCab-India-Routing/3.0 (dev@errorcab.com)")
                    .header("Accept", "application/json")
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && resp.body() != null && !resp.body().isBlank()) {
                JsonNode root = objectMapper.readTree(resp.body());
                if ("Ok".equalsIgnoreCase(root.path("code").asText())) {
                    JsonNode routes = root.path("routes");
                    if (routes.isArray() && !routes.isEmpty()) {
                        JsonNode firstRoute = routes.get(0);
                        double distanceMeters = firstRoute.path("distance").asDouble(0.0);
                        double durationSeconds = firstRoute.path("duration").asDouble(0.0);

                        double distanceKm = Math.round((distanceMeters / 1000.0) * 10.0) / 10.0;
                        int durationMins = (int) Math.max(5, Math.round(durationSeconds / 60.0));

                        return new RouteResult(
                                originName != null ? originName : "Origin",
                                destName != null ? destName : "Destination",
                                startLat, startLon, endLat, endLon,
                                distanceKm, durationMins, true, false,
                                "OSRM_ONLINE_ROUTING",
                                "Verified road routing via OpenStreetMap road network."
                        );
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "OSRM routing request failed: {0}", e.getMessage());
        }
        return null;
    }
}
