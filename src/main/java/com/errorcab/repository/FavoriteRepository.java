package com.errorcab.repository;

import com.errorcab.database.DatabaseManager;
import com.errorcab.model.FavoriteLocation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access repository for passenger favorite locations.
 */
public class FavoriteRepository {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public List<FavoriteLocation> getFavoritesByPassenger(int passengerId) {
        List<FavoriteLocation> list = new ArrayList<>();
        String sql = "SELECT * FROM favorites WHERE passenger_id = ? ORDER BY id ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new FavoriteLocation(
                            rs.getInt("id"),
                            rs.getInt("passenger_id"),
                            rs.getString("label"),
                            rs.getString("location_name"),
                            rs.getString("address"),
                            LocalDateTime.parse(rs.getString("created_at"))
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public FavoriteLocation addFavorite(FavoriteLocation fav) throws SQLException {
        String sql = "INSERT INTO favorites (passenger_id, label, location_name, address, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, fav.getPassengerId());
            ps.setString(2, fav.getLabel());
            ps.setString(3, fav.getLocationName());
            ps.setString(4, fav.getAddress());
            ps.setString(5, fav.getCreatedAt().toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    fav.setId(rs.getInt(1));
                }
            }
        }
        return fav;
    }

    public boolean deleteFavorite(int id, int passengerId) {
        String sql = "DELETE FROM favorites WHERE id = ? AND passenger_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, passengerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
