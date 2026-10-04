package com.errorcab.copilot.weather.service;

import com.errorcab.copilot.weather.model.WeatherResult;
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
 * Free weather provider using Open-Meteo public forecast API.
 * Never fabricates weather conditions.
 */
public class OpenMeteoWeatherProvider implements WeatherProvider {

    private static final Logger LOGGER = Logger.getLogger(OpenMeteoWeatherProvider.class.getName());
    private static final String API_URL = "https://api.open-meteo.com/v1/forecast";
    private static final Duration TIMEOUT = Duration.ofMillis(2000);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenMeteoWeatherProvider(HttpClient httpClient) {
        this.httpClient = httpClient != null ? httpClient : HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public OpenMeteoWeatherProvider() {
        this(null);
    }

    @Override
    public String getProviderName() {
        return "Open-Meteo Free Weather Service";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public WeatherResult getWeather(double latitude, double longitude) {
        if (latitude == 0.0 && longitude == 0.0) {
            return WeatherResult.unavailable();
        }

        try {
            String url = String.format(Locale.US,
                    "%s?latitude=%.4f&longitude=%.4f&current=temperature_2m,precipitation,weather_code",
                    API_URL, latitude, longitude);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "ERRORCab-India-Weather/3.0")
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && resp.body() != null && !resp.body().isBlank()) {
                JsonNode root = objectMapper.readTree(resp.body());
                JsonNode current = root.path("current");
                if (!current.isMissingNode()) {
                    double tempC = current.path("temperature_2m").asDouble(27.0);
                    double precipMm = current.path("precipitation").asDouble(0.0);
                    int code = current.path("weather_code").asInt(0);

                    String condition = mapWeatherCode(code);
                    boolean raining = precipMm > 0.5 || code >= 50;

                    return new WeatherResult(tempC, precipMm, condition, raining, "Open-Meteo");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Weather lookup failed: {0}", e.getMessage());
        }
        return WeatherResult.unavailable();
    }

    private String mapWeatherCode(int code) {
        if (code == 0) return "Clear Skies";
        if (code == 1 || code == 2) return "Partly Cloudy";
        if (code == 3) return "Overcast";
        if (code >= 45 && code <= 48) return "Foggy";
        if (code >= 51 && code <= 55) return "Light Drizzle";
        if (code >= 61 && code <= 65) return "Rain Showers";
        if (code >= 80 && code <= 82) return "Heavy Rain";
        if (code >= 95) return "Thunderstorms";
        return "Mild Tropical";
    }
}
