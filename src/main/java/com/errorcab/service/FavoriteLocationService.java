package com.errorcab.service;

import com.errorcab.model.FavoriteLocation;
import com.errorcab.repository.FavoriteRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

/**
 * Service managing passenger saved favorite locations (Home, Work, Favorite).
 */
@Service
public class FavoriteLocationService {
    private final FavoriteRepository favoriteRepo = new FavoriteRepository();

    public List<FavoriteLocation> getFavorites(int passengerId) {
        return favoriteRepo.getFavoritesByPassenger(passengerId);
    }

    public FavoriteLocation addFavorite(int passengerId, String label, String locationName, String address) throws SQLException {
        if (label == null || label.trim().isEmpty()) {
            label = "Favorite";
        }
        if (locationName == null || locationName.trim().isEmpty()) {
            throw new IllegalArgumentException("Location name is required.");
        }
        FavoriteLocation fav = new FavoriteLocation(passengerId, label.trim(), locationName.trim(), address != null ? address.trim() : locationName.trim());
        return favoriteRepo.addFavorite(fav);
    }

    public boolean deleteFavorite(int id, int passengerId) {
        return favoriteRepo.deleteFavorite(id, passengerId);
    }
}
