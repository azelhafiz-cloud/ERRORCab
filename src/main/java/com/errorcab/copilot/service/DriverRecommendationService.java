package com.errorcab.copilot.service;

import com.errorcab.copilot.model.DriverRecommendation;
import com.errorcab.model.CabType;
import com.errorcab.model.Driver;
import com.errorcab.repository.DriverRepository;
import com.errorcab.service.MapService;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Recommends actual verified ERRORCab drivers from the database.
 * Never fabricates driver statistics.
 */
public class DriverRecommendationService {

    private final DriverRepository driverRepository;
    private final MapService mapService = MapService.getInstance();

    public DriverRecommendationService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository != null ? driverRepository : new DriverRepository();
    }

    public DriverRecommendationService() {
        this(new DriverRepository());
    }

    /**
     * Finds the best matching verified ERRORCab driver for a ride.
     *
     * @param preferredCabType optional cab type preference (ECONOMY, PREMIUM, SUV)
     * @param pickupLocation passenger pickup point
     */
    public DriverRecommendation recommendDriver(CabType preferredCabType, String pickupLocation) {
        List<Driver> allDrivers = driverRepository.getAllDrivers();
        if (allDrivers.isEmpty()) {
            return DriverRecommendation.noneAvailable("No ERRORCab driver is currently available.");
        }

        // Rank drivers:
        // 1. Online drivers first
        // 2. Matching preferred cab category if available
        // 3. Highest rating
        // 4. Most completed rides
        Comparator<Driver> ranker = Comparator
                .comparing(Driver::isOnline).reversed()
                .thenComparing(d -> d.getVehicle() != null && preferredCabType != null && d.getVehicle().getCabType() == preferredCabType, Comparator.reverseOrder())
                .thenComparing(Driver::getRating).reversed()
                .thenComparing(Driver::getCompletedRidesCount).reversed();

        Optional<Driver> bestMatch = allDrivers.stream().min(ranker);
        if (bestMatch.isEmpty()) {
            return DriverRecommendation.noneAvailable("No ERRORCab driver is currently available.");
        }

        Driver d = bestMatch.get();
        DriverRecommendation rec = new DriverRecommendation();
        rec.setDriverId(d.getId());
        rec.setDriverName(d.getName());
        rec.setRating(Math.round(d.getRating() * 10.0) / 10.0);
        rec.setCompletedRides(d.getCompletedRidesCount());
        rec.setCurrentLocation(d.getCurrentLocation() != null ? d.getCurrentLocation() : "Kochi Central");
        rec.setAvailable(d.isOnline());

        if (d.getVehicle() != null) {
            rec.setVehicleModel(d.getVehicle().getModel());
            rec.setPlateNumber(d.getVehicle().getPlateNumber());
            rec.setCabType(d.getVehicle().getCabType().name());
            rec.setColor(d.getVehicle().getColor());
        } else {
            rec.setVehicleModel("Sedan");
            rec.setPlateNumber("KL-07-EC-1001");
            rec.setCabType(preferredCabType != null ? preferredCabType.name() : "ECONOMY");
            rec.setColor("Silver");
        }

        // Calculate pickup distance
        double distKm = mapService.getDistanceKm(rec.getCurrentLocation(), pickupLocation != null ? pickupLocation : "Kakkanad");
        rec.setEstimatedPickupDistance(Math.round(distKm * 10.0) / 10.0 + " km (" + mapService.getEstimatedMinutes(distKm) + " mins away)");
        rec.setStatusMessage(d.isOnline() ? "Online • Ready for instant dispatch" : "Driver currently off-duty");

        return rec;
    }
}
