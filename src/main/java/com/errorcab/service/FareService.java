package com.errorcab.service;

import com.errorcab.model.Cab;
import com.errorcab.model.CabType;
import com.errorcab.model.EconomyCab;
import com.errorcab.model.PremiumCab;
import com.errorcab.model.SUVCab;

import java.util.EnumMap;
import java.util.Map;

/**
 * Service managing cab pricing and polymorphic fare calculations.
 * Demonstrates Polymorphism and Strategy pattern.
 */
public class FareService {
    private static FareService instance;
    private final Map<CabType, Cab> cabCatalog = new EnumMap<>(CabType.class);

    private FareService() {
        // Register polymorphic Cab implementations
        cabCatalog.put(CabType.ECONOMY, new EconomyCab());
        cabCatalog.put(CabType.PREMIUM, new PremiumCab());
        cabCatalog.put(CabType.SUV, new SUVCab());
    }

    public static synchronized FareService getInstance() {
        if (instance == null) {
            instance = new FareService();
        }
        return instance;
    }

    public Cab getCab(CabType type) {
        return cabCatalog.get(type);
    }

    /**
     * Polymorphically calculates fare for the chosen Cab category.
     */
    public double calculateFare(CabType type, double distanceKm) {
        Cab cab = cabCatalog.get(type);
        if (cab != null) {
            return cab.calculateFare(distanceKm);
        }
        return 0.0;
    }

    public String getFareBreakdown(CabType type, double distanceKm) {
        Cab cab = cabCatalog.get(type);
        if (cab != null) {
            return cab.getFareBreakdown(distanceKm);
        }
        return "";
    }
}
