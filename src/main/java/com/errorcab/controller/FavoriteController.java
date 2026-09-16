package com.errorcab.controller;

import com.errorcab.model.FavoriteLocation;
import com.errorcab.service.FavoriteLocationService;
import com.errorcab.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Passenger Favorite Locations (Home, Work, Favorite).
 * Enforces ownership checks on passenger favorites.
 */
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    private final FavoriteLocationService favoriteService;
    private final SessionService sessionService;

    public FavoriteController(FavoriteLocationService favoriteService, SessionService sessionService) {
        this.favoriteService = favoriteService;
        this.sessionService = sessionService;
    }

    @GetMapping("/{passengerId}")
    public ResponseEntity<?> getFavorites(@PathVariable int passengerId, HttpServletRequest request) {
        if (!sessionService.isUserOrAdmin(request, passengerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied."));
        }
        return ResponseEntity.ok(favoriteService.getFavorites(passengerId));
    }

    @PostMapping
    public ResponseEntity<?> addFavorite(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            int passengerId = Integer.parseInt(body.get("passengerId").toString());
            if (!sessionService.isUserOrAdmin(request, passengerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied."));
            }

            String label = body.get("label").toString();
            String locationName = body.get("locationName").toString();
            String address = body.containsKey("address") && body.get("address") != null ? body.get("address").toString() : locationName;

            FavoriteLocation saved = favoriteService.addFavorite(passengerId, label, locationName, address);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/{passengerId}")
    public ResponseEntity<?> deleteFavorite(@PathVariable int id, @PathVariable int passengerId, HttpServletRequest request) {
        if (!sessionService.isUserOrAdmin(request, passengerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied."));
        }
        boolean ok = favoriteService.deleteFavorite(id, passengerId);
        return ResponseEntity.ok(Map.of("success", ok));
    }
}
