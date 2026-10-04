package com.errorcab.copilot.destination.service;

import com.errorcab.copilot.destination.model.DestinationProfile;
import com.errorcab.copilot.destination.model.DestinationResult;
import com.errorcab.copilot.model.DriverRecommendation;
import com.errorcab.copilot.routing.model.RouteResult;
import com.errorcab.copilot.routing.service.CompositeRoutingProvider;
import com.errorcab.copilot.routing.service.RoutingProvider;
import com.errorcab.copilot.service.DriverRecommendationService;
import com.errorcab.copilot.weather.model.WeatherResult;
import com.errorcab.copilot.weather.service.OpenMeteoWeatherProvider;
import com.errorcab.copilot.weather.service.WeatherProvider;
import com.errorcab.model.CabType;
import com.errorcab.model.Location;
import com.errorcab.service.FareService;
import com.errorcab.service.MapService;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Core destination intelligence service.
 * Coordinates destination resolution, geocoding, route calculation,
 * fare calculation, driver recommendation, and weather lookup.
 */
@Service
public class DestinationIntelligenceService {

    private final DestinationResolver destinationResolver;
    private final RoutingProvider routingProvider;
    private final FareService fareService = FareService.getInstance();
    private final MapService mapService = MapService.getInstance();
    private final DriverRecommendationService driverService;
    private final WeatherProvider weatherProvider;

    public DestinationIntelligenceService(DestinationResolver destinationResolver,
                                          RoutingProvider routingProvider,
                                          DriverRecommendationService driverService,
                                          WeatherProvider weatherProvider) {
        this.destinationResolver = destinationResolver != null ? destinationResolver : new DestinationResolver();
        this.routingProvider = routingProvider != null ? routingProvider : new CompositeRoutingProvider();
        this.driverService = driverService != null ? driverService : new DriverRecommendationService();
        this.weatherProvider = weatherProvider != null ? weatherProvider : new OpenMeteoWeatherProvider();
    }

    public DestinationIntelligenceService() {
        this(new DestinationResolver(), new CompositeRoutingProvider(), new DriverRecommendationService(), new OpenMeteoWeatherProvider());
    }

    /**
     * Resolves user query into a verified DestinationResult.
     */
    public DestinationResult resolveDestination(String query) {
        return destinationResolver.resolveDestination(query);
    }

    /**
     * Fetches or builds the rich DestinationProfile for the resolved destination.
     */
    public DestinationProfile getProfile(DestinationResult destResult) {
        if (destResult == null || !destResult.isResolved()) {
            return null;
        }
        return destinationResolver.resolve(destResult.getNormalizedPlaceName());
    }

    /**
     * Calculates road route between pickup point and resolved destination.
     */
    public RouteResult calculateRoute(String pickupLocation, DestinationResult destination) {
        double startLat = 10.0159; // Default Kakkanad / Kochi Hub
        double startLon = 76.3419;

        if (pickupLocation != null) {
            Optional<Location> locOpt = mapService.getLocation(pickupLocation);
            if (locOpt.isPresent()) {
                startLat = locOpt.get().getLatitude();
                startLon = locOpt.get().getLongitude();
            } else {
                // Check if pickup is a recognized destination in knowledge base
                DestinationProfile pickupProfile = DestinationKnowledgeBase.find(pickupLocation);
                if (pickupProfile != null && pickupProfile.getLatitude() != 0.0) {
                    startLat = pickupProfile.getLatitude();
                    startLon = pickupProfile.getLongitude();
                }
            }
        }

        double endLat = destination != null ? destination.getLatitude() : 0.0;
        double endLon = destination != null ? destination.getLongitude() : 0.0;

        return routingProvider.calculateRoute(
                startLat, startLon,
                endLat, endLon,
                pickupLocation != null ? pickupLocation : "Home Hub",
                destination != null ? destination.getNormalizedPlaceName() : "Destination"
        );
    }

    /**
     * Polymorphically calculates fares for Economy, Premium, and SUV cabs using FareService.
     */
    public Map<CabType, Double> calculateFares(double distanceKm) {
        Map<CabType, Double> fares = new EnumMap<>(CabType.class);
        fares.put(CabType.ECONOMY, fareService.calculateFare(CabType.ECONOMY, distanceKm));
        fares.put(CabType.PREMIUM, fareService.calculateFare(CabType.PREMIUM, distanceKm));
        fares.put(CabType.SUV, fareService.calculateFare(CabType.SUV, distanceKm));
        return fares;
    }

    /**
     * Recommends an authentic ERRORCab driver.
     */
    public DriverRecommendation recommendDriver(CabType cabType, String pickupLocation) {
        return driverService.recommendDriver(cabType, pickupLocation);
    }

    /**
     * Retrieves live destination weather.
     */
    public WeatherResult getWeather(DestinationResult destination) {
        if (destination == null || destination.getLatitude() == 0.0) {
            return WeatherResult.unavailable();
        }
        return weatherProvider.getWeather(destination.getLatitude(), destination.getLongitude());
    }

    public DestinationResolver getDestinationResolver() {
        return destinationResolver;
    }

    public RoutingProvider getRoutingProvider() {
        return routingProvider;
    }
}
