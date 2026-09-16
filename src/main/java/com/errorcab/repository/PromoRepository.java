package com.errorcab.repository;

import com.errorcab.database.DatabaseManager;
import com.errorcab.model.PromoCode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access repository for promo codes and discounts.
 */
public class PromoRepository {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public Optional<PromoCode> findByCode(String code) {
        if (code == null || code.trim().isEmpty()) return Optional.empty();

        String sql = "SELECT * FROM promo_codes WHERE UPPER(code) = ? AND active = 1";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new PromoCode(
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getDouble("discount_amount"),
                            rs.getDouble("minimum_fare"),
                            rs.getString("description"),
                            rs.getInt("active") == 1
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<PromoCode> getAllActivePromos() {
        List<PromoCode> list = new ArrayList<>();
        String sql = "SELECT * FROM promo_codes WHERE active = 1";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new PromoCode(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getDouble("discount_amount"),
                        rs.getDouble("minimum_fare"),
                        rs.getString("description"),
                        rs.getInt("active") == 1
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
